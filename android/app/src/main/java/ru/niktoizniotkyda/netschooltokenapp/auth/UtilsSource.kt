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

interface UtilsSource {
    suspend fun getYears(studentId: Int): List<SchoolYear>

    suspend fun getSubjects(
        studentId: Int,
        yearId: Int,
    ): List<Subject>


    suspend fun getTerms(studentId: Int, yearId: Int): List<Term>

    suspend fun getUsers(): List<UserInfo>
}
