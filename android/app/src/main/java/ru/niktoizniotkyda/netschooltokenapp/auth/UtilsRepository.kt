package ru.niktoizniotkyda.netschooltokenapp.auth

import kotlinx.coroutines.flow.StateFlow

interface UtilsRepository {
    val subjects: StateFlow<List<Subject>>

    suspend fun getYears(): List<SchoolYear>
    suspend fun getSubjects(yearId: Int)
    suspend fun getSubjectById(id: Int, yearId: Int): Subject?
    suspend fun getTerms(yearId: Int): List<Term>
    suspend fun getUsers(): List<UserInfo>
}