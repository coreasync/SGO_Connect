/*
 * Copyright 2024 Eugene Menshenin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ru.niktoizniotkyda.netschooltokenapp.auth

import android.content.Context
import com.google.gson.Gson
import ru.niktoizniotkyda.netschooltokenapp.di.BaseRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import javax.inject.Singleton
import ru.niktoizniotkyda.netschooltokenapp.data.AppDataStore

@Module
@InstallIn(SingletonComponent::class)
object BaseRetrofitModule {

    @Provides
    @Singleton
    @BaseRetrofit
    fun createRetrofit(
        gson: Gson,
        okHttpClient: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://localhost/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun createGson(): Gson = Gson()

    @Provides
    @Singleton
    fun createOkHttpClient(
        baseUrlInterceptor: BaseUrlInterceptor,
        headersInterceptor: HeadersInterceptor,
        loggingInterceptor: Interceptor,
    ): OkHttpClient {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

        return OkHttpClient.Builder()
            .addInterceptor(baseUrlInterceptor)
            .addInterceptor(headersInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun createHeadersInterceptor(dataStore: AppDataStore): HeadersInterceptor = HeadersInterceptor(dataStore)

    @Provides
    @Singleton
    fun createBaseUrlInterceptor(@ApplicationContext context: Context): BaseUrlInterceptor {
        return BaseUrlInterceptor(context)
    }

    @Provides
    @Singleton
    fun createLoggingInterceptor(): Interceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
}

