package ru.niktoizniotkyda.sgo_connectapp.auth

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import android.content.Context
import ru.niktoizniotkyda.sgo_connectapp.auth.calendar.CalendarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid
import ru.niktoizniotkyda.sgo_connectapp.data.AppDataStore
import ru.niktoizniotkyda.sgo_connectapp.data.TokenData
import kotlin.uuid.ExperimentalUuidApi

fun Long.toIso8601(): String = Instant.ofEpochMilli(this)
    .atOffset(ZoneOffset.UTC)
    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

object NetSchoolSingleton {
    private val _loggedIn = MutableStateFlow(false)
    val loggedIn: StateFlow<Boolean> = _loggedIn

    suspend fun isLoggedIn(boolean: Boolean) {
        _loggedIn.emit(boolean)
    }
}

@Singleton
class LoginRepositoryImpl @Inject constructor(
    val loginSource: LoginSource,
    private val dataStore: AppDataStore,
    val calendarRepository: CalendarRepository,
    @ApplicationContext val context: Context
) : LoginRepository {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun login(deviceCode: Int): String {
        val now = calendarRepository.getNow()
        val loginResponse = loginSource.getToken(deviceCode)

        val id = Uuid.random().toString()

        dataStore.addToken(
            TokenData.newBuilder()
                .setId(id)
                .setToken(loginResponse.accessToken)
                .setRefreshToken(loginResponse.refreshToken)
                .setTimeToRefresh(
                    (now + loginResponse.expiresIn * 1000L).toIso8601()
                )
                .build()
        )
        dataStore.setSelectedTokenId(id)

        NetSchoolSingleton.isLoggedIn(true)

        return id
    }
}
