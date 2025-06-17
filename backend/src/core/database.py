from sqlalchemy.orm import declarative_base
from sqlalchemy import Column, Integer, String
from fastapi.concurrency import asynccontextmanager
from sqlalchemy.ext.asyncio import create_async_engine
from sqlalchemy.ext.asyncio.session import AsyncSession, async_sessionmaker

from core.config import settings

class Database:
    def __init__(self):
        self.engine = create_async_engine(settings.DATABASE_URL, echo=True, future=True)
        self.Base = declarative_base()

    async def create_tables(self):
        async with self.engine.begin() as conn:
            await conn.run_sync(self.Base.metadata.create_all)
        print("Database tables created successfully")

    from typing import AsyncGenerator

    @asynccontextmanager
    async def get_session(self) -> AsyncGenerator[AsyncSession, None]:
        async_session = async_sessionmaker(self.engine, class_=AsyncSession)
        session = None
        try:
            session = async_session()
            async with session:
                yield session
        except Exception as e:
            if session is not None:
                await session.rollback()
            raise e
        finally:
            if session is not None:
                await session.close()
    
    async def close_database(self):
        await self.engine.dispose()

database = Database()

class User(database.Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    email = Column(String, unique=True, index=True)

async def setup_database():
    await database.create_tables()