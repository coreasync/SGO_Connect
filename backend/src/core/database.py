from datetime import datetime
from typing import Any

from sqlalchemy import Integer
from sqlalchemy import func
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy.ext.asyncio.session import AsyncSession
from sqlalchemy.ext.asyncio.session import async_sessionmaker
from sqlalchemy.orm import Mapped
from sqlalchemy.orm import class_mapper
from sqlalchemy.orm import declarative_base
from sqlalchemy.orm import mapped_column

from src.core.config import settings


def declarative_nested_model_constructor(self: Any, **kwargs: Any) -> None:
    cls_ = type(self)  # type: ignore

    relationships = class_mapper(cls_).relationships  # type: ignore
    composites = class_mapper(cls_).composites  # type: ignore

    for key, value in kwargs.items():
        if not hasattr(cls_, key) or value is None:  # type: ignore
            continue

        if isinstance(value, list):  # "one-to-many"
            if all(isinstance(elem, dict) for elem in value):  # type: ignore
                setattr(
                    self,
                    key,
                    [relationships[key].mapper.entity(**elem) for elem in value],  # type: ignore
                )
            else:
                setattr(self, key, value)
        elif isinstance(value, dict):  # "one-to-one"
            if key in relationships:
                setattr(self, key, relationships[key].mapper.entity(**value))
            if key in composites:
                setattr(self, key, composites[key].composite_class(**value))
        else:
            setattr(self, key, value)


class Database:
    def __init__(self):
        self.engine = create_async_engine(settings.DATABASE_URL,
                                          future=True,
                                          pool_size=settings.DATABASE_POOL_SIZE,
                                          max_overflow=8)
        self.Base = self._get_base()

    def _get_base(self):
        base = declarative_base(constructor=declarative_nested_model_constructor)

        class BaseModel(base):
            __abstract__ = True

            id: Mapped[int] = mapped_column(
                Integer,
                primary_key=True,
                autoincrement=True,
                unique=True,
                index=True,
                nullable=False,
            )
            created_at: Mapped[datetime] = mapped_column(server_default=func.now())
            updated_at: Mapped[datetime] = mapped_column(
                server_default=func.now(),
                onupdate=func.now(),
            )

        return BaseModel

    async def create_tables(self):
        async with self.engine.begin() as conn:
            await conn.run_sync(self.Base.metadata.create_all)

    async def get_session(self):
        session = async_sessionmaker(self.engine, class_=AsyncSession)()
        try:
            yield session
        finally:
            await session.close()

    async def close_database(self):
        await self.engine.dispose()


database = Database()


async def setup_database():
    await database.create_tables()
