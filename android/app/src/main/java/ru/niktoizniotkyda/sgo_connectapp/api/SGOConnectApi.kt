package ru.niktoizniotkyda.sgo_connectapp.api

import retrofit2.Response
import retrofit2.http.*

import ru.niktoizniotkyda.sgo_connectapp.data.TokenData

interface SGOConnectApi {

    /**
     * Создание токена
     * POST /v1/tokens/
     */
    @POST("v1/tokens/")
    suspend fun createToken(
        @Body tokenInput: TokenData
    ): Response<TokenID>

    /**
     * Получение токена по ID
     * GET /v1/tokens/{token_id}
     */
    @GET("v1/tokens/{token_id}")
    suspend fun getToken(
        @Path("token_id") tokenId: String
    ): Response<TokenOutput>
}