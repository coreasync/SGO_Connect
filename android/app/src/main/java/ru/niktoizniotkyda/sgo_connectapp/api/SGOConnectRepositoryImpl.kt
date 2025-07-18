package ru.niktoizniotkyda.sgo_connectapp.api

import com.google.protobuf.util.JsonFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.niktoizniotkyda.sgo_connectapp.data.TokenData
import javax.inject.Inject


fun TokenData.toRequestBody(): RequestBody {
    val json = JsonFormat.printer()
        .includingDefaultValueFields()
        .preservingProtoFieldNames()
        .print(this)  // protobuf -> json string
    return json.toRequestBody("application/json".toMediaType())
}

class SGOConnectRepositoryImpl
    @Inject
    constructor(
    var api: SGOConnectApi
) : SGOConnectRepository {
    override suspend fun createToken(tokenInput: TokenData): Result<TokenID> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createToken(tokenInput.toRequestBody())

                if (response.isSuccessful) {
                    response.body()?.let { tokenId ->
                        Result.success(tokenId)
                    } ?: Result.failure(Exception("Пустой ответ от сервера"))
                } else {
                    // Обработка различных HTTP кодов ошибок
                    when (response.code()) {
                        422 -> Result.failure(ValidationException("Ошибка валидации данных"))
                        500 -> Result.failure(ServerException("Внутренняя ошибка сервера"))
                        else -> Result.failure(ApiException("Ошибка API: ${response.code()}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(NetworkException("Ошибка сети: ${e.message}"))
            }
        }
    }
}