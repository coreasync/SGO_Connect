from collections.abc import Awaitable
from collections.abc import Callable
from contextvars import ContextVar
from enum import Enum
from functools import wraps
from inspect import currentframe
from time import perf_counter
from time import time
from typing import Any
from typing import TypeVar

import psutil
from pydantic import BaseModel
from pydantic import Field

from src.core.log import log_context
from src.core.log import logger
from src.utils.app_exceptions import AppExceptionCase
from src.utils.app_exceptions import ErrorSeverity

service_chain_context: ContextVar[list[str] | None] = ContextVar(
    "service_chain",
    default=None,
)

if service_chain_context.get() is None:
    service_chain_context.set([])

T = TypeVar("T")
E = TypeVar("E", bound=AppExceptionCase)


class ResultStatus(str, Enum):
    SUCCESS = "success"
    ERROR = "error"
    TIMEOUT = "timeout"
    CANCELLED = "cancelled"


class ServiceMetrics(BaseModel):
    start_time: float = Field(default_factory=perf_counter)
    end_time: float | None = None
    duration_ms: float | None = None
    memory_usage_mb: float | None = None

    def finish(self) -> None:
        self.end_time = perf_counter()
        self.duration_ms = round((self.end_time - self.start_time) * 1000, 2)

        try:
            process = psutil.Process()
            self.memory_usage_mb = round(process.memory_info().rss / 1024 / 1024, 2)
        except ImportError:
            pass


class ServiceResult[T]:
    def __init__(
        self,
        value: T | AppExceptionCase,
        service_name: str | None = None,
        operation_name: str | None = None,
        metrics: ServiceMetrics | None = None,
        timestamp: float = time(),
    ) -> None:
        self._value = value
        self.service_name = service_name
        self.operation_name = operation_name
        self.metrics = metrics or ServiceMetrics()
        self.timestamp = timestamp

        if isinstance(value, AppExceptionCase):
            self.status = ResultStatus.ERROR
            self.exception = value
            self.success = False
        else:
            self.status = ResultStatus.SUCCESS
            self.exception = None
            self.success = True

    @property
    def value(self) -> T:
        if not self.success:
            raise RuntimeError(
                "Cannot access value of failed ServiceResult. Use is_success() check first.",
            )
        return self._value  # type: ignore

    @property
    def error(self) -> AppExceptionCase | None:
        return self.exception

    def is_success(self) -> bool:
        return self.success

    def is_error(self) -> bool:
        return not self.success

    def get_value_or_none(self) -> T | None:
        return self._value if self.success else None  # type: ignore

    def get_value_or_default(self, default: T) -> T:
        return self._value if self.success else default  # type: ignore

    def finish_metrics(self) -> "ServiceResult[T]":
        self.metrics.finish()
        return self

    def __str__(self) -> str:
        status_str = f"[{self.status.value.upper()}]"
        if self.service_name:
            status_str += f" {self.service_name}"
        if self.operation_name:
            status_str += f".{self.operation_name}"

        if not self.success and self.exception is not None:
            status_str += f" - {self.exception.get_error_code()}"

        if self.metrics.duration_ms is not None:
            status_str += f" ({self.metrics.duration_ms}ms)"

        return status_str

    def __repr__(self) -> str:
        return f"<ServiceResult status={self.status.value} service={self.service_name}>"


class ServiceResultFactory:
    def __init__(self, service_name: str, default_request_id: str | None = None):
        self.service_name = service_name
        self.default_request_id = default_request_id

    def success(self, value: T, operation_name: str | None = None) -> ServiceResult[T]:
        return ServiceResult(
            value,
            service_name=self.service_name,
            operation_name=operation_name,
        )

    def error(
        self,
        exception: AppExceptionCase,
        operation_name: str | None = None,
    ) -> ServiceResult[Any]:
        return ServiceResult(
            exception,
            service_name=self.service_name,
            operation_name=operation_name,
        )


def get_caller_info(skip_frames: int = 2) -> dict[str, Any]:
    frame = currentframe()
    for _ in range(skip_frames):
        frame = frame.f_back if frame else None

    if frame:
        code = frame.f_code
        return {
            "filename": code.co_filename.split("/")[-1],
            "function": code.co_name,
            "lineno": frame.f_lineno,
        }

    return {"filename": "unknown", "function": "unknown", "lineno": 0}


async def handle_service_result[T](result: ServiceResult[T]) -> T:
    if result.metrics.end_time is None:
        result.finish_metrics()
    with log_context(result.service_name, result.operation_name):
        if result.is_error():
            log_data: dict[str, Any] = {
                "error_code": result.exception.get_error_code() if result.exception else None,
                "status_code": result.exception.status_code if result.exception else None,
                "severity": result.exception.severity.value if result.exception else None,
                "service_name": result.service_name,
                "operation_name": result.operation_name,
                "duration_ms": result.metrics.duration_ms,
                "memory_usage_mb": result.metrics.memory_usage_mb,
            }

            if result.exception is not None:
                if result.exception.severity is ErrorSeverity.CRITICAL:
                    logger.critical("Service operation failed", **log_data)
                elif result.exception.severity is ErrorSeverity.HIGH:
                    logger.error("Service operation failed", **log_data)
                elif result.exception.severity is ErrorSeverity.MEDIUM:
                    logger.warning("Service operation failed", **log_data)
                elif result.exception.severity is ErrorSeverity.LOW:
                    logger.info("Service operation failed", **log_data)

            if result.exception is not None:
                raise result.exception
            raise RuntimeError("ServiceResult is error but exception is None.")

        if result.service_name and result.metrics.duration_ms:
            logger.success(
                "Service operation completed",
                service_name=result.service_name,
                operation_name=result.operation_name,
                duration_ms=result.metrics.duration_ms,
                memory_usage_mb=result.metrics.memory_usage_mb,
            )

    return result.value


def handle_result[T](
    func: Callable[..., Awaitable[ServiceResult[T]]],
) -> Callable[..., Awaitable[T]]:
    @wraps(func)
    async def wrapper(*args: Any, **kwargs: Any) -> T:
        service_name = getattr(func, "__module__", "unknown").split(".")[-1]
        operation_name = func.__name__

        current_chain = service_chain_context.get([]) or []
        new_chain = [*current_chain, f"{service_name}.{operation_name}"]

        service_chain_token = service_chain_context.set(new_chain)

        try:
            result: ServiceResult[T] = await func(*args, **kwargs)

            if not result.service_name:
                result.service_name = service_name
            if not result.operation_name:
                result.operation_name = operation_name

            return await handle_service_result(result)

        finally:
            service_chain_context.reset(service_chain_token)

    return wrapper


def success[T](value: T, service_name: str | None = None) -> ServiceResult[T]:
    return ServiceResult(value, service_name=service_name)


def error(
    exception: AppExceptionCase,
    service_name: str | None = None,
) -> ServiceResult[Any]:
    return ServiceResult(exception, service_name=service_name)
