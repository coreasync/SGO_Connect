from fastapi import FastAPI
from fastapi import Request
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException

from src.utils.app_exceptions import AppExceptionCase
from src.utils.app_exceptions import app_exception_handler
from src.utils.request_exceptions import http_exception_handler
from src.utils.request_exceptions import request_validation_exception_handler


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(StarletteHTTPException)
    async def custom_http_exception_handler(
        request: Request,
        e: StarletteHTTPException,
    ):  # type: ignore
        return await http_exception_handler(request, e)

    @app.exception_handler(RequestValidationError)
    async def custom_validation_exception_handler(
        request: Request,
        e: RequestValidationError,
    ):  # type: ignore
        return await request_validation_exception_handler(request, e)

    @app.exception_handler(AppExceptionCase)
    async def custom_app_exception_handler(request: Request, e: AppExceptionCase):  # type: ignore
        return await app_exception_handler(request, e)
