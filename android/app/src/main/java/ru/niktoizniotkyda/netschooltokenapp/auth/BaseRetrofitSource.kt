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

import com.google.gson.JsonParseException
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import okio.IOException
import retrofit2.HttpException
import ru.niktoizniotkyda.netschooltokenapp.utils.BackendException
import ru.niktoizniotkyda.netschooltokenapp.utils.ConnectionException
import ru.niktoizniotkyda.netschooltokenapp.utils.ParseBackendResponseException
import ru.niktoizniotkyda.netschooltokenapp.utils.TimeOutError
import java.net.SocketTimeoutException
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
open class BaseRetrofitSource
@Inject
constructor(
    baseRetrofitConfig: RetrofitConfig
) {
    private val errorAdapter = baseRetrofitConfig.gson.getAdapter(Error::class.java)


    suspend fun <T> wrapRetrofitExceptions(
        loggingRequest: Boolean = false,
        block: suspend () -> T,
    ): T {
        return try {
            if (!loggingRequest){
                val loggedIn = NetSchoolSingleton.loggedIn
                while (!loggedIn.first()) {
                    delay(1)
                }
            }
            block()
        } catch (e: JsonParseException) {
            throw ParseBackendResponseException(e)
        } catch (e: HttpException) {
            throw createBackendException(e)
        } catch (e: SocketTimeoutException) {
            throw TimeOutError(e)
        } catch (e: IOException) {
            throw ConnectionException(e)
        }
    }

    private fun createBackendException(e: HttpException): Exception {
        return try {
            val errorBody = errorAdapter.fromJson(e.message())

            BackendException(errorBody.message!!)
        } catch (e: Exception) {
            throw ParseBackendResponseException(e)
        }
    }
}
