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

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitLoginSource
    @Inject
    constructor(
        retrofitConfig: RetrofitConfig,
    ) : BaseRetrofitSource(retrofitConfig), LoginSource {
        private val api = retrofitConfig.loginRetrofit.create(IdentityLoginApi::class.java)

        override suspend fun getToken(deviceCode: Int): GetTokenResponse =
            wrapRetrofitExceptions(true) {
                api.getToken(
                    grantType = "urn:ietf:params:oauth:grant-type:device_code",
                    deviceCode = deviceCode,
                )
            }

    override suspend fun getToken(refreshToken: String): GetTokenResponse =
            wrapRetrofitExceptions(true) {
                api.getToken(
                    grantType = "refresh_token",
                    refreshToken = refreshToken,
                )
            }

    override suspend fun getUsers(): List<UserInfo> =
            wrapRetrofitExceptions {
                api.getUsers()
            }
}
