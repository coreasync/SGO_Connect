package ru.niktoizniotkyda.netschooltokenapp.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.niktoizniotkyda.netschooltokenapp.utils.toDescription
import javax.inject.Inject
import ru.niktoizniotkyda.netschooltokenapp.data.AppDataStore
import ru.niktoizniotkyda.netschooltokenapp.data.UserData
import ru.niktoizniotkyda.netschooltokenapp.data.OrganizationInfo
import ru.niktoizniotkyda.netschooltokenapp.data.Clazz
import ru.niktoizniotkyda.netschooltokenapp.data.Organization
import ru.niktoizniotkyda.netschooltokenapp.data.Children
import ru.niktoizniotkyda.netschooltokenapp.data.ChildOrNull

fun <T> MutableLiveData<T>.toLiveData(): LiveData<T> = this


@HiltViewModel
class GosuslugiResultViewModel
@Inject
constructor(
    private val loginRepository: LoginRepositoryImpl,
    private val utilsRepository: UtilsRepositoryImpl,
    private val dataStore: AppDataStore
) : ViewModel() {

    private val _loggedIn = MutableLiveData<Boolean?>(null)
    val loggedIn = _loggedIn.toLiveData()

    private val _users = MutableLiveData<List<UserInfo>>()
    val users = _users.toLiveData()

    private val _error = MutableLiveData<String>()
    val error = _error.toLiveData()

    suspend fun login(code: Int) {
        try {
            val tokenId = loginRepository.login(code)
            val getUsers = utilsRepository.getUsers()

            if (getUsers.isNotEmpty()) {
                val protoUsers = getUsers.map { user ->
                    UserData.newBuilder()
                        .setUserId(user.id)
                        .setFirstName(user.firstName)
                        .setNickName(user.nickName)
                        .setLoginName(user.loginName)
                        .setIsParent(user.isParent)
                        .setIsStaff(user.isStaff)
                        .setIsStudent(user.isStudent)
                        .addAllOrganizations(user.organizations.map { orgInfo ->
                            OrganizationInfo.newBuilder()
                                .addAllClasses(orgInfo.classes.map { clazz ->
                                    Clazz.newBuilder()
                                        .setClassId(clazz.classId)
                                        .setClassName(clazz.className)
                                        .build()
                                })
                                .setIsActive(orgInfo.isActive)
                                .setOrganization(
                                    Organization.newBuilder()
                                        .setId(orgInfo.organization.id)
                                        .setIsAddSchool(orgInfo.organization.isAddSchool)
                                        .setName(orgInfo.organization.name)
                                        .build()
                                )
                                .build()
                        })
                        .addAllChildren(
                            user.children.orEmpty().map { child ->
                                ChildOrNull.newBuilder()
                                    .setChild(
                                        Children.newBuilder()
                                            .setChildrenId(child.id)
                                            .setFirstName(child.firstName)
                                            .setNickName(child.nickName)
                                            .setLoginName(child.loginName)
                                            .setIsParent(child.isParent)
                                            .setIsStaff(child.isStaff)
                                            .setIsStudent(child.isStudent)
                                            .addAllOrganizations(child.organizations.map { orgInfo ->
                                                OrganizationInfo.newBuilder()
                                                    .addAllClasses(orgInfo.classes.map { clazz ->
                                                        Clazz.newBuilder()
                                                            .setClassId(clazz.classId)
                                                            .setClassName(clazz.className)
                                                            .build()
                                                    })
                                                    .setIsActive(orgInfo.isActive)
                                                    .setOrganization(
                                                        Organization.newBuilder()
                                                            .setId(orgInfo.organization.id)
                                                            .setIsAddSchool(orgInfo.organization.isAddSchool)
                                                            .setName(orgInfo.organization.name)
                                                            .build()
                                                    )
                                                    .build()
                                            })
                                            .build()
                                    )
                                    .build()
                            }
                        )
                        .build()
                }

                dataStore.updateTokenUsers(tokenId, protoUsers)

                dataStore.setSelectedTokenId(tokenId)
                dataStore.setSelectedUserId(getUsers[0].id)
            }
            withContext(Dispatchers.Main) {
                _users.value = getUsers
                _loggedIn.value = true
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                e.printStackTrace()
                _loggedIn.value = false
                _error.value = e.toDescription()
            }
        }
    }
}
