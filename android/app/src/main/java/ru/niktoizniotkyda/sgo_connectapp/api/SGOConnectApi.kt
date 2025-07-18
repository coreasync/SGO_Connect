package ru.niktoizniotkyda.sgo_connectapp.api

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface SGOConnectApi {
    @POST("v1/tokens/")
    suspend fun createToken(
        @Body data: RequestBody
    ): Response<TokenID>
}