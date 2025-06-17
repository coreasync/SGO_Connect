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

import retrofit2.http.GET
import retrofit2.http.Query

interface UtilsApi {
    @GET("api/mobile/education")
    suspend fun getEducation(
        @Query("studentId") studentId: Int,
    ): List<EducationInfo>

    @GET("api/mobile/subjects")
    suspend fun getSubjects(
        @Query("studentId") studentId: Int,
        @Query("schoolYearId") yearId: Int,
    ): List<Subject>

    @GET("api/mobile/terms")
    suspend fun getTerms(
        @Query("studentId") studentId: Int,
        @Query("schoolYearId") yearId: Int
    ): List<Term>


    @GET("api/mobile/users?v=2")
    suspend fun getUsers(): List<UserInfo>
}
