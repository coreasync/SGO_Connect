package ru.niktoizniotkyda.sgo_connectapp.auth

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.niktoizniotkyda.sgo_connectapp.api.SGOConnectRepository
import ru.niktoizniotkyda.sgo_connectapp.api.SGOConnectRepositoryImpl
import ru.niktoizniotkyda.sgo_connectapp.data.AppDataStore
import ru.niktoizniotkyda.sgo_connectapp.data.AppDataStoreImpl
import ru.niktoizniotkyda.sgo_connectapp.auth.calendar.CalendarRepository
import ru.niktoizniotkyda.sgo_connectapp.auth.calendar.CalendarRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLoginRepository(
        loginRepositoryImpl: LoginRepositoryImpl
    ): LoginRepository

    @Binds
    @Singleton
    abstract fun bindUtilsRepository(
        utilsRepositoryImpl: UtilsRepositoryImpl
    ): UtilsRepository

    @Binds
    @Singleton
    abstract fun bindLoginSource(
        retrofitLoginSource: RetrofitLoginSource
    ): LoginSource

    @Binds
    @Singleton
    abstract fun bindUtilsSource(
        retrofitUtilsSource: RetrofitUtilsSource
    ): UtilsSource

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        calendarRepositoryImpl: CalendarRepositoryImpl
    ): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindSGOConnectRepository(
        sgoConnectRepositoryImpl: SGOConnectRepositoryImpl
    ): SGOConnectRepository

    @Binds
    abstract fun bindAppDataStore(
        impl: AppDataStoreImpl
    ): AppDataStore
}