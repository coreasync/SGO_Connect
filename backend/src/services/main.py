from sqlalchemy.ext.asyncio.session import AsyncSession


class DBSessionMixin:
    def __init__(self, session: AsyncSession):
        self.session = session


class AppService(DBSessionMixin):
    pass


class AppCRUD(DBSessionMixin):
    async def save(self) -> None:
        await self.session.commit()
