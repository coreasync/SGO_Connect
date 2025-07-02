import logging
from asyncio import iscoroutinefunction
from collections.abc import Callable
from contextlib import contextmanager
from contextvars import ContextVar
from functools import wraps
from json import dumps as json_dumps
from sys import stdout
from time import perf_counter
from typing import TYPE_CHECKING
from typing import Any
from typing import TypeVar
from typing import cast

from loguru import logger

if TYPE_CHECKING:
    from loguru import Record

from src.core.config import Environment
from src.core.config import LogLevel
from src.core.config import settings

T = TypeVar("T", bound=Callable[..., Any])

service_name_ctx: ContextVar[str | None] = ContextVar("service_name", default=None)
operation_name_ctx: ContextVar[str | None] = ContextVar("operation_name", default=None)


class BaseClass:
    _SERVICE_NAME: str

    def get_log_context(self, operation_name: str):
        return log_context(self._SERVICE_NAME, operation_name)


def safe_serialize(obj: object) -> str:
    try:
        if isinstance(obj, str):
            result = obj
        elif isinstance(obj, (dict, list, tuple)):
            result = json_dumps(obj, ensure_ascii=False, default=str)
        else:
            result = str(obj)
    except Exception:
        return "<serialization_error>"
    else:
        return result


def escape_special_chars(text: str) -> str:
    replacements = {
        "{": "{{",
        "}": "}}",
        "<": "&lt;",
        ">": "&gt;",
    }

    for old, new in replacements.items():
        text = text.replace(old, new)

    return text


def development_formatter(record: "Record") -> str:
    level_colors: dict[str, str] = {
        "TRACE": "\033[90m",
        "DEBUG": "\033[36m",
        "INFO": "\033[32m",
        "SUCCESS": "\033[32;1m",
        "WARNING": "\033[33m",
        "ERROR": "\033[31m",
        "CRITICAL": "\033[31;1m",
    }
    reset = "\033[0m"
    level_color = level_colors.get(record["level"].name, reset)
    time_str = record["time"].strftime("%H:%M:%S.%f")[:-3]

    context_parts: list[str] = []

    service_name = service_name_ctx.get()
    operation_name = operation_name_ctx.get()

    if service_name:
        context_parts.append(f"svc:{escape_special_chars(str(service_name))}")

    if operation_name:
        context_parts.append(f"op:{escape_special_chars(str(operation_name))}")

    context_str = f"[{' | '.join(context_parts)}] " if context_parts else ""

    safe_message = escape_special_chars(str(record["message"]))

    log_line = (
        f"{level_colors['TRACE']}{time_str}{reset} | "
        f"{level_color}{record['level'].name:<8}{reset} | "
        f"{level_colors['TRACE']}{record['name']}:{record['function']}:{record['line']}{reset} | "
        f"{context_str}"
        f"{safe_message}"
    )

    if record.get("extra"):
        try:
            extra_items: list[str] = []
            for k, v in record["extra"].items():
                try:
                    safe_key = escape_special_chars(str(k)[:50])
                    safe_value = escape_special_chars(safe_serialize(v))
                    extra_items.append(f"{safe_key}={safe_value}")
                except Exception:
                    extra_items.append(
                        f"{escape_special_chars(str(k))}=<unserializable>",
                    )

            if extra_items:
                extra_str = " | ".join(extra_items)
                log_line += f" | {level_colors['TRACE']}{extra_str}{reset}"
        except Exception:
            log_line += f" | {level_colors['TRACE']}extra=<error>{reset}"

    return log_line + "\n"


def performance_filter(record: "Record") -> bool:
    if settings.ENVIRONMENT == Environment.PRODUCTION:
        return record["level"].no >= logger.level("INFO").no
    return True


def init_logging() -> None:
    logger.remove()

    if settings.ENABLE_CONSOLE:
        if not settings.ENABLE_JSON:
            logger.add(
                stdout,
                format=development_formatter,
                level=settings.LOG_LEVEL.value,
                colorize=False,
                backtrace=True,
                diagnose=True,
                filter=performance_filter,
                catch=True,
            )
        else:
            logger.add(
                stdout,
                serialize=True,
                level=settings.LOG_LEVEL.value,
                colorize=False,
                backtrace=False,
                diagnose=False,
                filter=performance_filter,
                catch=True,
            )

    class InterceptHandler(logging.Handler):
        def emit(self, record: logging.LogRecord) -> None:
            try:
                level = logger.level(record.levelname).name
            except ValueError:
                level = record.levelno

            if record.levelno < logger.level(LogLevel.DEBUG.value).no:
                return

            frame, depth = logging.currentframe(), 2
            while frame and frame.f_code.co_filename == logging.__file__:
                frame = frame.f_back
                depth += 1

            logger.opt(depth=depth, exception=record.exc_info).log(
                level,
                record.getMessage(),
            )

    logging.basicConfig(handlers=[InterceptHandler()], level=40, force=True)

    logger.debug(
        "Logging configured successfully",
        environment=settings.ENVIRONMENT.value,
        level=settings.LOG_LEVEL.value,
        console_enabled=settings.ENABLE_CONSOLE,
        json_format=settings.ENABLE_JSON,
    )


class LogContext:
    @staticmethod
    def set_service_context(
        service_name: str,
        operation_name: str | None = None,
    ) -> None:
        service_name_ctx.set(service_name)
        if operation_name:
            operation_name_ctx.set(operation_name)

    @staticmethod
    def clear_context() -> None:
        service_name_ctx.set(None)
        operation_name_ctx.set(None)

    @staticmethod
    def get_context() -> dict[str, str | None]:
        return {
            "service_name": service_name_ctx.get(),
            "operation_name": operation_name_ctx.get(),
        }


@contextmanager
def log_context(
    service_name: str | None = None,
    operation_name: str | None = None,
):
    """Context manager for setting service and operation names in logs.

    Args:
        service_name (Optional[str], optional): The name of the service. Defaults to None.
        operation_name (Optional[str], optional): The name of the operation. Defaults to None.

    Usage:
        with log_context(service_name="service", operation_name="operation"):
            logger.info("message")
    """
    old_service_name = service_name_ctx.get()
    old_operation_name = operation_name_ctx.get()

    try:
        if service_name:
            service_name_ctx.set(service_name)
        if operation_name:
            operation_name_ctx.set(operation_name)

        yield
    finally:
        service_name_ctx.set(old_service_name)
        operation_name_ctx.set(old_operation_name)


def log_function_calls(
    level: str = LogLevel.TRACE.value,
    *,
    include_args: bool = False,
    include_result: bool = False,
) -> Callable[[Callable[..., Any]], Callable[..., Any]]:
    """Decorator for automatic logging of function calls.

    Args:
        level (str, optional): The logging level. Defaults to LogLevel.TRACE.value.
        include_args (bool, optional): Whether to include function arguments in the logs. Defaults to False.
        include_result (bool, optional): Whether to include the function result in the logs. Defaults to False.

    Usage:
        @log_function_calls(level="INFO", include_args=True)
        async def process_user_data(user_id: str):
            pass

    Returns:
        Callable[[Callable[..., Any]], Callable[..., Any]]: A decorator for logging function calls.
    """

    def decorator(func: T) -> T:
        @wraps(func)
        async def async_wrapper(*args: Any, **kwargs: Any):
            function_name = f"{func.__module__}.{func.__name__}"

            with log_context(operation_name=func.__name__):
                start_time = perf_counter()

                log_data = {"function": function_name}
                if include_args:
                    if args:
                        log_data["args"] = safe_serialize(args)
                    if kwargs:
                        log_data["kwargs"] = safe_serialize(kwargs)

                logger.log(level, f"Function call started: {function_name}", **log_data)

                try:
                    result = await func(*args, **kwargs)
                    duration_ms = round((perf_counter() - start_time) * 1000, 2)

                    success_data: dict[str, Any] = {
                        "function": function_name,
                        "duration_ms": duration_ms,
                    }
                    if include_result:
                        success_data["result"] = safe_serialize(result)

                    logger.log(
                        level,
                        f"Function call completed: {function_name}",
                        **success_data,
                    )
                except Exception as e:
                    duration_ms = round((perf_counter() - start_time) * 1000, 2)
                    logger.error(
                        f"Function call failed: {function_name}",
                        function=function_name,
                        duration_ms=duration_ms,
                        error=safe_serialize(str(e)),
                        error_type=type(e).__name__,
                    )
                    raise
                else:
                    return result

        @wraps(func)
        def sync_wrapper(*args: Any, **kwargs: Any):
            function_name = f"{func.__module__}.{func.__name__}"

            with log_context(operation_name=func.__name__):
                start_time = perf_counter()

                log_data = {"function": function_name}
                if include_args:
                    if args:
                        log_data["args"] = safe_serialize(args)
                    if kwargs:
                        log_data["kwargs"] = safe_serialize(kwargs)

                logger.log(level, f"Function call started: {function_name}", **log_data)

                try:
                    result = func(*args, **kwargs)
                    duration_ms = round((perf_counter() - start_time) * 1000, 2)

                    success_data: dict[str, Any] = {
                        "function": function_name,
                        "duration_ms": duration_ms,
                    }
                    if include_result:
                        success_data["result"] = safe_serialize(result)

                    logger.log(
                        level,
                        f"Function call completed: {function_name}",
                        **success_data,
                    )
                except Exception as e:
                    duration_ms = round((perf_counter() - start_time) * 1000, 2)
                    logger.error(
                        f"Function call failed: {function_name}",
                        function=function_name,
                        duration_ms=duration_ms,
                        error=safe_serialize(str(e)),
                        error_type=type(e).__name__,
                    )
                    raise
                else:
                    return result

        return cast("T", async_wrapper if iscoroutinefunction(func) else sync_wrapper)

    return decorator


init_logging()

__all__ = [
    "BaseClass",
    "LogContext",
    "init_logging",
    "log_context",
    "log_function_calls",
    "logger",
]
