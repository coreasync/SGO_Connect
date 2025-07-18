package ru.niktoizniotkyda.sgo_connectapp.api

data class ErrorResponse(
    val error_code: String,
    val message: String,
    val details: Map<String, Any>? = null,
    val timestamp: Long? = null
)

data class ValidationError(
    val loc: List<Any>,
    val msg: String,
    val type: String
)

data class HTTPValidationError(
    val detail: List<ValidationError>
)