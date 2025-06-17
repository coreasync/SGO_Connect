from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PROJECT_NAME: str = "NetSchoolToken"
    PORT: int
    VERSION: str = "0.0.1a"
    API_VERSION_PREFIX: str = "/v1"
    TOKEN_TIMEOUT: int = 10 * 60  # in seconds

    POSTGRES_USER: str
    POSTGRES_PASSWORD: str
    POSTGRES_HOST: str
    POSTGRES_PORT: int
    POSTGRES_DB: str
    DATABASE_URL: str

settings = Settings() # type: ignore