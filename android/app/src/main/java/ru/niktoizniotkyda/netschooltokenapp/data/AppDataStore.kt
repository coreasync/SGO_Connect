package ru.niktoizniotkyda.netschooltokenapp.data

import javax.inject.Singleton

@Singleton
interface AppDataStore {
    suspend fun getAppSettings(): AppSettings
    suspend fun setSelectedUserId(userId: Int)
    suspend fun setSelectedTokenId(tokenId: String)
    suspend fun addToken(tokenData: TokenData)
    suspend fun clearTokens()
    suspend fun getSelectedTokenData(): TokenData?
    suspend fun updateTokenUsers(tokenId: String, newUsers: List<UserData>)
    suspend fun writeTestSettings(settings: AppSettings)
}

