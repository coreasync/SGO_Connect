from enum import Enum
from hashlib import sha256 as hashlib_sha256

from pydantic import computed_field
from pydantic_settings import BaseSettings

_PROJECT_NAME = "NetSchoolToken"
_VERSION = "0.0.1a"
_API_VERSION_PREFIX = "/v1"


class Environment(str, Enum):
    DEVELOPMENT = "development"
    TESTING = "testing"
    PRODUCTION = "production"


class LogLevel(str, Enum):
    TRACE = "TRACE"
    DEBUG = "DEBUG"
    INFO = "INFO"
    SUCCESS = "SUCCESS"
    WARNING = "WARNING"
    ERROR = "ERROR"
    CRITICAL = "CRITICAL"


no = {
    LogLevel.TRACE: 5,
    LogLevel.DEBUG: 10,
    LogLevel.INFO: 20,
    LogLevel.SUCCESS: 25,
    LogLevel.WARNING: 30,
    LogLevel.ERROR: 40,
    LogLevel.CRITICAL: 50,
}


class Settings(BaseSettings):
    @computed_field
    @property
    def PROJECT_NAME(self) -> str:  # noqa: N802
        return _PROJECT_NAME

    @computed_field
    @property
    def VERSION(self) -> str:  # noqa: N802
        return _VERSION

    @computed_field
    @property
    def API_VERSION_PREFIX(self) -> str:  # noqa: N802
        return _API_VERSION_PREFIX

    @computed_field
    @property
    def DATABASE_URL(self) -> str:  # noqa: N802
        return f"postgresql+asyncpg://{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}@{self.POSTGRES_HOST}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}"

    ENVIRONMENT: Environment = Environment.DEVELOPMENT
    LOG_LEVEL: LogLevel = LogLevel.TRACE
    ENABLE_CONSOLE: bool = True
    ENABLE_JSON: bool = False
    PORT: int
    TOKEN_EXPIRES_SECONDS: int = 5 * 60  # 5 minutes
    SECRET_KEY: str
    SALT: bytes
    POSTGRES_USER: str
    POSTGRES_PASSWORD: str
    POSTGRES_HOST: str
    POSTGRES_PORT: int
    POSTGRES_DB: str


settings = Settings()  # type: ignore
settings.SALT = hashlib_sha256(f"{settings.SECRET_KEY}".encode()).digest()[:16]
