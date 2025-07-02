from collections.abc import Awaitable
from collections.abc import Callable
from time import perf_counter

from fastapi import FastAPI
from fastapi import Request
from fastapi import Response

from src.core.log import log_context
from src.core.log import logger


def register_middleware(app: FastAPI) -> None:
    @app.middleware("http")
    async def logging_middleware(  # type: ignore
        request: Request,
        call_next: Callable[[Request], Awaitable[Response]],
    ):
        with log_context(service_name="api"):
            logger.info(
                "HTTP request started",
                method=request.method,
                path=str(request.url.path),
                query_params=dict(request.query_params),
                user_agent=request.headers.get("User-Agent"),
                client_ip=request.client.host if request.client else None,
            )
            start_time = perf_counter()
            try:
                response: Response = await call_next(request)
                duration_ms = round((perf_counter() - start_time) * 1000, 2)
                logger.info(
                    "HTTP request completed",
                    status_code=response.status_code,
                    duration_ms=duration_ms,
                )
            except Exception as e:
                duration_ms = round((perf_counter() - start_time) * 1000, 2)
                logger.error(
                    "HTTP request failed",
                    error=str(e),
                    error_type=type(e).__name__,
                    duration_ms=duration_ms,
                )
                raise
            else:
                return response
