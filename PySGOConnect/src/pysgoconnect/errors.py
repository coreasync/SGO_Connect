from typing import Any

from pydantic import BaseModel
from pydantic import Field


class BaseErrorSchema(BaseModel):
    error_code: str = Field(..., description="Client error code")
    message: str = Field(..., description="Human-readable error message")
    details: dict[str, Any] | None = Field(None, description="Additional error details")
    timestamp: float = Field(description="Unix timestamp of the error")


class TransmissionProtocolSecurityError(Exception):
    """Исключение, возникающее при нарушении безопасности протокола передачи."""


class TokenValidationError(Exception):
    """Исключение, возникающее при ошибке валидации токена."""

    def __init__(
        self,
        error_code: str,
        message: str,
        details: dict[str, Any] | None = None,
        timestamp: float | None = None,
    ):
        self.error_code = error_code
        self.message = message
        self.details = details
        self.timestamp = timestamp
        super().__init__(message)


class TokenNotFoundError(Exception):
    """Исключение, возникающее при отсутствии или истечении срока действия токена."""

    def __init__(
        self,
        error_code: str,
        message: str,
        details: dict[str, Any] | None = None,
        timestamp: float | None = None,
    ):
        self.error_code = error_code
        self.message = message
        self.details = details
        self.timestamp = timestamp
        super().__init__(message)


class NoResponseFromServerError(Exception):
    """Исключение, возникающее при отсутствии ответа от сервера."""
