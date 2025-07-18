package ru.niktoizniotkyda.sgo_connectapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.flow.first
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataStoreImpl @Inject constructor(
    private val dataStore: DataStore<AppSettings>
) : AppDataStore {
    @Module
    @InstallIn(SingletonComponent::class)
    object DataStoreModule {

        @Provides
        @Singleton
        fun provideAppSettingsDataStore(
            @ApplicationContext context: Context
        ): DataStore<AppSettings> =
            DataStoreFactory.create(
                serializer = AppSettingsSerializer,
                produceFile = { context.dataStoreFile("app_settings.pb") }
            )
    }

    override suspend fun getAppSettings(): AppSettings = dataStore.data.first()

    override suspend fun setSelectedUserId(userId: Int) {
        dataStore.updateData { current ->
            current.toBuilder()
                .setSelectedUserId(userId)
                .build()
        }
    }

    override suspend fun setSelectedTokenId(tokenId: String) {
        dataStore.updateData { current ->
            current.toBuilder()
                .setSelectedToken(tokenId)
                .build()
        }
    }

    override suspend fun addToken(tokenData: TokenData) {
        dataStore.updateData { current ->
            current.toBuilder()
                .addTokens(tokenData)
                .build()
        }
    }

    override suspend fun clearTokens() {
        dataStore.updateData { current ->
            current.toBuilder()
                .clearTokens()
                .build()
        }
    }

    override suspend fun getSelectedTokenData(): TokenData? {
        val settings = dataStore.data.first()
        return settings.tokensList.find { it.id == settings.selectedToken }
    }

    override suspend fun updateTokenUsers(tokenId: String, newUsers: List<UserData>) {
        val settings = dataStore.data.first()
        val updatedTokens = settings.tokensList.map { token ->
            if (token.id == tokenId) {
                token.toBuilder()
                    .clearUsers()
                    .addAllUsers(newUsers)
                    .build()
            } else token
        }

        val updatedSettings = settings.toBuilder()
            .clearTokens()
            .addAllTokens(updatedTokens)
            .build()

        dataStore.updateData { updatedSettings }
    }

    override suspend fun writeTestSettings(settings: AppSettings) {
        dataStore.updateData { settings }
    }
}