package ru.niktoizniotkyda.sgo_connectapp.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

import ru.niktoizniotkyda.sgo_connectapp.R
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Предоставляет интерцептор для логирования HTTP запросов и ответов
     * Аннотация @Provides говорит Hilt, что этот метод предоставляет зависимость
     * @Singleton гарантирует, что будет создан только один экземпляр
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Предоставляет настроенный OkHttpClient
     * Hilt автоматически передаст HttpLoggingInterceptor в качестве параметра
     */
    @Provides
    @Singleton
    @Named("networkClient")
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Предоставляет настроенный Gson
     * Отдельный провайдер позволяет легко менять настройки сериализации
     */
    @Provides
    @Singleton
    @Named("networkGson")
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
            .create()
    }

    /**
     * Предоставляет экземпляр Retrofit
     * @ApplicationContext - это специальная аннотация Hilt для получения контекста приложения
     * Теперь мы можем безопасно использовать context.getString() для получения URL из ресурсов
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(context.getString(R.string.server_url))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Предоставляет готовый к использованию API интерфейс
     * Hilt автоматически создаст Retrofit и передаст его в этот метод
     */
    @Provides
    @Singleton
    fun provideSGOConnectApi(retrofit: Retrofit): SGOConnectApi {
        return retrofit.create(SGOConnectApi::class.java)
    }
}