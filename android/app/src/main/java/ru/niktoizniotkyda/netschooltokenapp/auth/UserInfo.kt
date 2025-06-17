package ru.niktoizniotkyda.netschooltokenapp.auth

import com.google.gson.annotations.SerializedName

data class Clazz(
    @SerializedName("classId")
    val classId: Int,
    @SerializedName("className")
    val className: String
)

data class Organization(
    @SerializedName("id")
    val id: Int,
    @SerializedName("isAddSchool")
    val isAddSchool: Boolean,
    @SerializedName("name")
    val name: String
)

data class OrganizationInfo(
    @SerializedName("classes")
    val classes: List<Clazz>,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("organization")
    val organization: Organization
)

data class Children(
    @SerializedName("id")
    val id: Int,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("nickName")
    val nickName: String,
    @SerializedName("loginName")
    val loginName: String,
    @SerializedName("isParent")
    val isParent: Boolean,
    @SerializedName("isStaff")
    val isStaff: Boolean,
    @SerializedName("isStudent")
    val isStudent: Boolean,
    @SerializedName("organizations")
    val organizations: List<OrganizationInfo>
)

data class UserInfo(
    @SerializedName("id")
    val id: Int,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("nickName")
    val nickName: String,
    @SerializedName("loginName")
    val loginName: String,
    @SerializedName("isParent")
    val isParent: Boolean,
    @SerializedName("isStaff")
    val isStaff: Boolean,
    @SerializedName("isStudent")
    val isStudent: Boolean,
    @SerializedName("organizations")
    val organizations: List<OrganizationInfo>,
    @SerializedName("children")
    val children: List<Children>? = null
)