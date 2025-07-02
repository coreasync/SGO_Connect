import time
from enum import Enum
from typing import Any
from typing import ClassVar

from fastapi import Request
from fastapi import status
from pydantic import BaseModel
from pydantic import Field
from starlette.responses import JSONResponse

from src.core.log import logger


class ErrorSeverity(str, Enum):
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"


class ErrorResponse(BaseModel):
    error_code: str = Field(..., description="Client error code")
    message: str = Field(..., description="Human-readable error message")
    details: dict[str, Any] | None = Field(None, description="Additional error details")
    timestamp: float = Field(
        default_factory=time.time,
        description="Unix timestamp of the error",
    )

    class Config:
        json_encoders: ClassVar[dict[type, Any]] = {
            float: (lambda v: round(v, 3)),  # type: ignore
        }


class AppExceptionCase(Exception):  # noqa: N818
    status_code: ClassVar[int] = status.HTTP_500_INTERNAL_SERVER_ERROR
    error_code: ClassVar[str] = "INTERNAL_ERROR"
    message: ClassVar[str] = "Internal server error"
    severity: ClassVar[ErrorSeverity] = ErrorSeverity.HIGH
    should_log: ClassVar[bool] = True
    expose_details: ClassVar[bool] = False

    def __init__(
        self,
        details: dict[str, Any] | None = None,
        message: str | None = None,
    ):
        self.details = details or {}
        self.custom_message = message
        self.timestamp = time.time()

        super().__init__(self.get_message())

    def get_message(self) -> str:
        return self.custom_message or self.message

    def get_error_code(self) -> str:
        return self.error_code

    def to_response_model(self, *, expose_details: bool = False) -> ErrorResponse:
        return ErrorResponse(
            error_code=self.get_error_code(),
            message=self.get_message(),
            details=self.details if (expose_details or self.expose_details) else None,
            timestamp=self.timestamp,
        )

    @classmethod
    def get_response_schema(
        cls,
        description: str | None = None,
    ) -> dict[int | str, dict[str, Any]]:
        return {
            cls.status_code: {
                "model": ErrorResponse,
                "description": description or cls.message,
            },
        }

    def __str__(self) -> str:
        return (
            f"<{self.__class__.__name__} "
            f"code={self.get_error_code()} "
            f"status={self.status_code} "
            f"message='{self.get_message()}'>"
        )


async def app_exception_handler(
    request: Request,
    exc: AppExceptionCase,
) -> JSONResponse:
    if exc.should_log:
        log_data: dict[str, Any] = {
            "error_code": exc.get_error_code(),
            "status_code": exc.status_code,
            "severity": exc.severity.value,
            "path": str(request.url.path),
            "method": request.method,
            "user_agent": request.headers.get("User-Agent"),
            "client_ip": request.client.host if request.client else None,
        }

        if exc.details:
            log_data["details"] = exc.details

        if exc.severity is ErrorSeverity.CRITICAL:
            logger.critical("Application exception occurred", **log_data)
        elif exc.severity is ErrorSeverity.HIGH:
            logger.error("Application exception occurred", **log_data)
        elif exc.severity is ErrorSeverity.MEDIUM:
            logger.warning("Application exception occurred", **log_data)
        elif exc.severity is ErrorSeverity.LOW:
            logger.info("Application exception occurred", **log_data)

    error_response = exc.to_response_model()

    return JSONResponse(
        status_code=exc.status_code,
        content=error_response.model_dump(exclude_none=True),
    )


class TokenException:
    class TokenCreateError(AppExceptionCase):
        status_code = status.HTTP_500_INTERNAL_SERVER_ERROR
        error_code = "TOKEN_CREATE_FAILED"
        message = "Failed to create token"
        severity = ErrorSeverity.HIGH

    class TokenValidationError(AppExceptionCase):
        status_code = status.HTTP_400_BAD_REQUEST
        error_code = "TOKEN_INVALID"
        message = "Token validation failed"
        severity = ErrorSeverity.LOW
        expose_details = True

    class TokenNotFoundError(AppExceptionCase):
        status_code = status.HTTP_404_NOT_FOUND
        error_code = "TOKEN_NOT_FOUND"
        message = "Token not found or expired"
        severity = ErrorSeverity.LOW


class ExceptionRegistry:
    _exceptions: ClassVar[dict[str, type[AppExceptionCase]]] = {}

    @classmethod
    def register(
        cls,
        exception_class: type[AppExceptionCase],
    ) -> type[AppExceptionCase]:
        cls._exceptions[exception_class.error_code] = exception_class
        return exception_class

    @classmethod
    def get_all_schemas(cls) -> dict[int | str, dict[str, Any]]:
        return {
            code: schema
            for exc_class in cls._exceptions.values()
            for code, schema in exc_class.get_response_schema().items()
        }

    @classmethod
    def get_exception_by_code(cls, error_code: str) -> type[AppExceptionCase] | None:
        return cls._exceptions.get(error_code)


for attr_name in dir(TokenException):
    attr = getattr(TokenException, attr_name)
    if isinstance(attr, type) and issubclass(attr, AppExceptionCase) and attr != AppExceptionCase:
        ExceptionRegistry.register(attr)


def create_exception(
    status_code: int,
    error_code: str,
    message: str,
    severity: ErrorSeverity,
    *,
    should_log: bool = True,
    expose_details: bool = False,
) -> type[AppExceptionCase]:
    class DynamicException(AppExceptionCase):
        pass

    DynamicException.status_code = status_code
    DynamicException.error_code = error_code
    DynamicException.message = message
    DynamicException.severity = severity
    DynamicException.should_log = should_log
    DynamicException.expose_details = expose_details

    ExceptionRegistry.register(DynamicException)

    return DynamicException
