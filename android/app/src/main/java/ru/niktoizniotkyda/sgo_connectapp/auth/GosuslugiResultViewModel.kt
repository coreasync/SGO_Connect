package ru.niktoizniotkyda.sgo_connectapp.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.niktoizniotkyda.sgo_connectapp.utils.toDescription
import javax.inject.Inject
import ru.niktoizniotkyda.sgo_connectapp.data.AppDataStore
import ru.niktoizniotkyda.sgo_connectapp.data.UserData
import ru.niktoizniotkyda.sgo_connectapp.data.OrganizationInfo
import ru.niktoizniotkyda.sgo_connectapp.data.Class
import ru.niktoizniotkyda.sgo_connectapp.data.Organization
import ru.niktoizniotkyda.sgo_connectapp.data.Child

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
                                .addAllClasses(orgInfo.classes.map { Class_ ->
                                    Class.newBuilder()
                                        .setClassId(Class_.classId)
                                        .setClassName(Class_.className)
                                        .build()
                                })
                                .setIsActive(orgInfo.isActive)
                                .setOrganization(
                                    Organization.newBuilder()
                                        .setOrganizationId(orgInfo.organization.id)
                                        .setIsAddSchool(orgInfo.organization.isAddSchool)
                                        .setName(orgInfo.organization.name)
                                        .build()
                                )
                                .build()
                        })
                        .addAllChildren(
                    user.children.orEmpty().map { child ->
                        Child.newBuilder()
                                    .setChildId(child.id.toLong())
                                    .setFirstName(child.firstName)
                                    .setNickName(child.nickName)
                                    .setLoginName(child.loginName)
                                    .setIsParent(child.isParent)
                                    .setIsStaff(child.isStaff)
                                    .setIsStudent(child.isStudent)
                                    .addAllOrganizations(child.organizations.map { orgInfo ->
                                        OrganizationInfo.newBuilder()
                                            .addAllClasses(orgInfo.classes.map { Class_ ->
                                                Class.newBuilder()
                                                    .setClassId(Class_.classId)
                                                    .setClassName(Class_.className)
                                                    .build()
                                            })
                                            .setIsActive(orgInfo.isActive)
                                            .setOrganization(
                                                Organization.newBuilder()
                                                    .setOrganizationId(orgInfo.organization.id)
                                                    .setIsAddSchool(orgInfo.organization.isAddSchool)
                                                    .setName(orgInfo.organization.name)
                                                    .build()
                                            )
                                            .build()
                                    })
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
