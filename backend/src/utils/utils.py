from typing import TypeVar

from pydantic import BaseModel
from sqlalchemy.orm import DeclarativeBase

ModelType = TypeVar("ModelType", bound=DeclarativeBase)
CreateSchemaType = TypeVar("CreateSchemaType", bound=BaseModel)


def pydantic_to_sqlalchemy[CreateSchemaType: BaseModel, ModelType: DeclarativeBase](
    pydantic_instance: type[CreateSchemaType],
    sqlalchemy_model: type[ModelType],
) -> ModelType:
    try:
        data = pydantic_instance.model_dump(exclude_unsest=True, exclude_none=True)  # type: ignore
        return sqlalchemy_model(**data)
    except Exception as e:
        raise ValueError(
            f"Error converting Pydantic model to SQLAlchemy model: {e}",
        ) from e
