from datetime import datetime

from pydantic import BaseModel
from pydantic import ConfigDict
from pydantic import Field


class BaseSchema(BaseModel):
    model_config = ConfigDict(from_attributes=True)


class Class(BaseSchema):
    class_id: int
    class_name: str = Field(..., max_length=256)


class Organization(BaseSchema):
    organization_id: int
    is_add_school: bool
    name: str = Field(..., max_length=1024)


class OrganizationInfo(BaseSchema):
    is_active: bool
    classes: list[Class] = Field(default_factory=list[Class])
    organization: Organization


class Child(BaseSchema):
    child_id: int
    first_name: str = Field(..., max_length=256)
    nick_name: str = Field(..., max_length=256)
    login_name: str = Field(..., max_length=256)
    is_parent: bool
    is_staff: bool
    is_student: bool
    organizations: list[OrganizationInfo]


class User(BaseSchema):
    user_id: int
    first_name: str = Field(..., max_length=256)
    nick_name: str = Field(..., max_length=256)
    login_name: str = Field(..., max_length=256)
    is_parent: bool
    is_staff: bool
    is_student: bool
    organizations: list[OrganizationInfo]
    children: list[Child] | None = None


class Token(BaseSchema):
    refresh_token: str = Field(..., max_length=16384)
    time_to_refresh: datetime
    users: list[User]


class TokenID(BaseSchema):
    token_id: str = Field(
        ...,
        min_length=36,
        max_length=36,
        description="Token ID in UUID format",
    )
    expires_at: datetime = Field(..., description="Token expiration time")
    token_expires_seconds: int = Field(
        ...,
        description="Token expiration time in seconds",
    )
