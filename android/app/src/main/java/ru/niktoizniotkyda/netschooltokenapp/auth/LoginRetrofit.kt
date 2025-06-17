package ru.niktoizniotkyda.netschooltokenapp.auth

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import ru.niktoizniotkyda.netschooltokenapp.di.LoginRetrofit

@Module
@InstallIn(SingletonComponent::class)
object LoginRetrofitModule {

    @Provides
    @Singleton
    @LoginRetrofit
    fun createLoginRetrofit(
        gson: Gson,
        @LoginRetrofit okHttpClient: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://identity.ir-tech.ru/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @LoginRetrofit
    fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            })
            .build()
    }
}
