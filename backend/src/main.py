from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI

from src.core.config import Environment
from src.core.config import settings
from src.core.database import database
from src.core.database import setup_database
from src.models import *  # noqa: F403
from src.routers import main_router
from src.utils.exception_handlers import register_exception_handlers
from src.utils.middlewares import register_middleware


@asynccontextmanager
async def lifespan(app: FastAPI):  # noqa: ARG001
    await setup_database()
    yield
    await database.close_database()


app = FastAPI(
    version=settings.VERSION,
    title=settings.PROJECT_NAME,
    description=f"Documentation to {settings.PROJECT_NAME}",
    docs_url="/docsdev" if settings.ENVIRONMENT != Environment.PRODUCTION else None,
    redoc_url=f"{settings.API_VERSION_PREFIX}/docs",
    openapi_url=f"{settings.API_VERSION_PREFIX}/openapi.json",
    lifespan=lifespan,
)

register_middleware(app)
register_exception_handlers(app)

app.include_router(main_router, prefix=settings.API_VERSION_PREFIX)

if __name__ == "__main__":
    uvicorn.run(
        app,
        host="0.0.0.0",  # noqa: S104
        port=settings.PORT,
        log_config=None,
    )
