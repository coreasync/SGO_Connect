package ru.niktoizniotkyda.sgo_connectapp.api

/**
 * Исключение при ошибке валидации данных
 * Обычно соответствует HTTP коду 400 или 422
 */
class ValidationException(message: String) : Exception(message)

/**
 * Исключение при внутренней ошибке сервера
 * Соответствует HTTP коду 500
 */
class ServerException(message: String) : Exception(message)

/**
 * Общее исключение для ошибок API
 * Используется для неизвестных HTTP кодов
 */
class ApiException(message: String) : Exception(message)

/**
 * Исключение при проблемах с сетевым соединением
 * Возникает при отсутствии интернета или недоступности сервера
 */
class NetworkException(message: String) : Exception(message)
