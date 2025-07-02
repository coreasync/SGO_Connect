from typing import Annotated

from fastapi import APIRouter
from fastapi import Depends
from fastapi import status
from fastapi.responses import Response

from src.core.database import AsyncSession
from src.core.database import database
from src.core.log import LogLevel
from src.core.log import log_function_calls
from src.schemas.token import Token as TokenSchema
from src.schemas.token import TokenID
from src.services.tokens import TokenService
from src.utils.app_exceptions import TokenException

router = APIRouter(prefix="/tokens", tags=["tokens"])


@router.post(
    "/",
    response_model=TokenID,
    status_code=status.HTTP_201_CREATED,
    responses=TokenException.TokenCreateError.get_response_schema(),
)
@log_function_calls(level=LogLevel.INFO.value)
async def create_token(token: TokenSchema, session: Annotated[AsyncSession, Depends(database.get_session)]):
    return Response(
        content=(await TokenService(session).create_token(token)).model_dump_json(),
        status_code=status.HTTP_201_CREATED,
    )


@router.get(
    "/{token_id}",
    response_model=TokenSchema,
    responses={
        **TokenException.TokenValidationError.get_response_schema(),
        **TokenException.TokenNotFoundError.get_response_schema(),
    },
)
@log_function_calls(level=LogLevel.INFO.value)
async def get_token(token_id: str, session: Annotated[AsyncSession, Depends(database.get_session)]):
    return await TokenService(session).get_token(token_id)
