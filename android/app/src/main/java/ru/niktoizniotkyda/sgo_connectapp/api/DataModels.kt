package ru.niktoizniotkyda.sgo_connectapp.api

data class TokenID(
    val token_id: String,
    val expires_at: String,
    val token_expires_seconds: Int
)
