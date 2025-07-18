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

package ru.niktoizniotkyda.sgo_connectapp.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitUtilsSource
@Inject
constructor(
    config: RetrofitConfig,
) : UtilsSource, BaseRetrofitSource(config) {
    private val utilsApi = config.baseRetrofit.create(UtilsApi::class.java)

    override suspend fun getYears(studentId: Int) =
        wrapRetrofitExceptions {
            utilsApi.getEducation(studentId).filter { !it.isAddSchool }.map { it.schoolYear }
        }

    override suspend fun getSubjects(
        studentId: Int,
        yearId: Int,
    ) = wrapRetrofitExceptions {
        utilsApi.getSubjects(studentId, yearId)
    }

    override suspend fun getTerms(studentId: Int, yearId: Int): List<Term> =
        wrapRetrofitExceptions {
            utilsApi.getTerms(studentId, yearId)
        }

    override suspend fun getUsers(): List<UserInfo> = wrapRetrofitExceptions {
        utilsApi.getUsers()
    }
}
