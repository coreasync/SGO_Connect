package ru.niktoizniotkyda.netschooltokenapp.auth.calendar

data class WeekStartEndEntity(
    val weekStart: String,
    val weekEnd: String,
    val formattedWeekStart: String? = null,
    val formattedWeekEnd: String? = null
)