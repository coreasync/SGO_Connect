from datetime import UTC
from datetime import datetime
from datetime import timedelta

from sqlalchemy import select

from src.core.config import settings
from src.models.token import Token as TokenModel
from src.schemas.token import Token as TokenSchema
from src.schemas.token import TokenID
from src.services.main import AppCRUD
from src.services.main import AppService
from src.utils import pydantic_to_sqlalchemy
from src.utils.app_exceptions import TokenException
from src.utils.encrypting import UUIDGeneratorError
from src.utils.encrypting import uuid_generator
from src.utils.service_result import ServiceResult
from src.utils.service_result import error
from src.utils.service_result import handle_result
from src.utils.service_result import success


class TokenService(AppService):
    @handle_result
    async def create_token(self, token: TokenSchema) -> ServiceResult[TokenID]:
        try:
            expires_at = datetime.now(UTC) + timedelta(
                seconds=settings.TOKEN_EXPIRES_SECONDS,
            )
            created_token = await TokenCRUD(self.session).create_token(
                token,
                expires_at,
            )
            token_id = TokenID(
                token_id=uuid_generator.int_to_uuid(created_token),
                expires_at=expires_at,
                token_expires_seconds=settings.TOKEN_EXPIRES_SECONDS,
            )

        except Exception as e:
            return error(
                TokenException.TokenCreateError(
                    details={"token": token.model_dump_json(), "error": str(e)},
                ),
            )

        return success(token_id)

    @handle_result
    async def get_token(self, token_id: str) -> ServiceResult[TokenSchema]:
        try:
            id = uuid_generator.uuid_to_int(token_id)

        except UUIDGeneratorError as e:
            return ServiceResult(
                TokenException.TokenValidationError(
                    details={"token_id": token_id, "error": str(e)},
                ),
            )

        token_model = await TokenCRUD(self.session).get_item(id)

        if not token_model:
            return ServiceResult(
                TokenException.TokenNotFoundError(details={"token_id": str(token_id)}),
            )

        return ServiceResult(TokenSchema.model_validate(token_model))


class TokenCRUD(AppCRUD):
    async def create_token(self, token: TokenSchema, expires_at: datetime) -> int:
        db_token = pydantic_to_sqlalchemy(token, TokenModel)
        db_token.expires_at = expires_at

        self.session.add(db_token)

        await self.save()
        await self.session.refresh(db_token)

        return db_token.id

    async def get_item(self, token_id: int) -> TokenModel | None:
        now = datetime.now(UTC)
        return (
            await self.session.scalars(
                select(TokenModel).where(
                    TokenModel.id == token_id,
                    TokenModel.expires_at > now,
                    TokenModel.time_to_refresh > now,
                ),
            )
        ).first()
