package ru.niktoizniotkyda.sgo_connectapp.auth.calendar

data class WeekStartEndEntity(
    val weekStart: String,
    val weekEnd: String,
    val formattedWeekStart: String? = null,
    val formattedWeekEnd: String? = null
)