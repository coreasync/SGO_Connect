from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI

from core.config import settings
from core.database import database, setup_database

@asynccontextmanager
async def lifespan(app: FastAPI):
    await setup_database()
    yield
    await database.close_database()

def main():
    app = FastAPI(
        version=settings.VERSION,
        title=settings.PROJECT_NAME,
        description=f"Documentation to {settings.PROJECT_NAME}",
        docs_url="/d",
        redoc_url=f"{settings.API_VERSION_PREFIX}/docs",
        openapi_url=f"{settings.API_VERSION_PREFIX}/openapi.json",
        lifespan=lifespan
    )

    uvicorn.run(
        app,
        host="0.0.0.0",
        port=settings.PORT
    )


if __name__ == "__main__":
    main()
