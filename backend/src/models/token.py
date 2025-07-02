from datetime import datetime

from sqlalchemy import Boolean
from sqlalchemy import DateTime
from sqlalchemy import ForeignKey
from sqlalchemy import Integer
from sqlalchemy import String
from sqlalchemy.orm import Mapped
from sqlalchemy.orm import mapped_column
from sqlalchemy.orm import relationship

from src.core.database import database


class ClassUser(database.Base):
    __tablename__ = "classes_users"

    class_id: Mapped[int] = mapped_column(Integer, nullable=False)
    class_name: Mapped[str] = mapped_column(String(256), nullable=False)

    organization_info_id: Mapped[int] = mapped_column(
        ForeignKey("organization_infos_users.id"),
    )
    organization_info: Mapped["OrganizationInfoUser"] = relationship(
        "OrganizationInfoUser",
        back_populates="classes",
    )


class ClassChild(database.Base):
    __tablename__ = "classes_children"

    class_id: Mapped[int] = mapped_column(Integer, nullable=False)
    class_name: Mapped[str] = mapped_column(String(256), nullable=False)

    organization_info_id: Mapped[int] = mapped_column(
        ForeignKey("organization_infos_children.id"),
    )
    organization_info: Mapped["OrganizationInfoChild"] = relationship(
        "OrganizationInfoChild",
        back_populates="classes",
    )


class OrganizationUser(database.Base):
    __tablename__ = "organizations_users"

    organization_id: Mapped[int] = mapped_column(Integer, nullable=False)
    is_add_school: Mapped[bool] = mapped_column(Boolean, nullable=False)
    name: Mapped[str] = mapped_column(String(1024), nullable=False)

    organization_info_id: Mapped[int] = mapped_column(
        ForeignKey("organization_infos_users.id"),
    )
    organization_info: Mapped["OrganizationInfoUser"] = relationship(
        "OrganizationInfoUser",
        back_populates="organization",
    )


class OrganizationChild(database.Base):
    __tablename__ = "organizations_children"

    organization_id: Mapped[int] = mapped_column(Integer, nullable=False)
    is_add_school: Mapped[bool] = mapped_column(Boolean, nullable=False)
    name: Mapped[str] = mapped_column(String(1024), nullable=False)

    organization_info_id: Mapped[int] = mapped_column(
        ForeignKey("organization_infos_children.id"),
    )
    organization_info: Mapped["OrganizationInfoChild"] = relationship(
        "OrganizationInfoChild",
        back_populates="organization",
    )


class OrganizationInfoUser(database.Base):
    __tablename__ = "organization_infos_users"

    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False)

    classes: Mapped[list["ClassUser"]] = relationship(
        "ClassUser",
        lazy="joined",
        back_populates="organization_info",
    )

    organization: Mapped["OrganizationUser"] = relationship(
        "OrganizationUser",
        lazy="joined",
        uselist=False,
        back_populates="organization_info",
    )

    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"))
    user: Mapped["User"] = relationship("User", back_populates="organizations")


class OrganizationInfoChild(database.Base):
    __tablename__ = "organization_infos_children"

    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False)

    classes: Mapped[list["ClassChild"]] = relationship(
        "ClassChild",
        lazy="joined",
        back_populates="organization_info",
    )

    organization: Mapped["OrganizationChild"] = relationship(
        "OrganizationChild",
        lazy="joined",
        uselist=False,
        back_populates="organization_info",
    )

    child_id: Mapped[int] = mapped_column(ForeignKey("children.id"))
    child: Mapped["Child"] = relationship("Child", back_populates="organizations")


class Child(database.Base):
    __tablename__ = "children"

    child_id: Mapped[int] = mapped_column(Integer, nullable=False)

    first_name: Mapped[str] = mapped_column(String(256), nullable=False)
    nick_name: Mapped[str] = mapped_column(String(256), nullable=False)
    login_name: Mapped[str] = mapped_column(String(256), nullable=False)

    is_parent: Mapped[bool] = mapped_column(Boolean, nullable=False)
    is_staff: Mapped[bool] = mapped_column(Boolean, nullable=False)
    is_student: Mapped[bool] = mapped_column(Boolean, nullable=False)

    organizations: Mapped[list["OrganizationInfoChild"]] = relationship(
        "OrganizationInfoChild",
        lazy="joined",
        back_populates="child",
    )

    user_id: Mapped[int] = mapped_column(ForeignKey("users.id"))
    user: Mapped["User"] = relationship("User", back_populates="children")


class User(database.Base):
    __tablename__ = "users"

    user_id: Mapped[int] = mapped_column(Integer, nullable=False)

    first_name: Mapped[str] = mapped_column(String(256), nullable=False)
    nick_name: Mapped[str] = mapped_column(String(256), nullable=False)
    login_name: Mapped[str] = mapped_column(String(256), nullable=False)

    is_parent: Mapped[bool] = mapped_column(Boolean, nullable=False)
    is_staff: Mapped[bool] = mapped_column(Boolean, nullable=False)
    is_student: Mapped[bool] = mapped_column(Boolean, nullable=False)

    children: Mapped[list["Child"]] = relationship(
        "Child",
        lazy="joined",
        back_populates="user",
    )
    organizations: Mapped[list["OrganizationInfoUser"]] = relationship(
        "OrganizationInfoUser",
        lazy="joined",
        back_populates="user",
    )

    token_id: Mapped[int] = mapped_column(ForeignKey("tokens.id"))
    token: Mapped["Token"] = relationship("Token", back_populates="users")


class Token(database.Base):
    __tablename__ = "tokens"

    refresh_token: Mapped[str] = mapped_column(String(16384), nullable=False)
    time_to_refresh: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
    )

    users: Mapped[list["User"]] = relationship(
        "User",
        lazy="joined",
        back_populates="token",
    )

    expires_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
    )
