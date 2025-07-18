package ru.niktoizniotkyda.sgo_connectapp.api


import ru.niktoizniotkyda.sgo_connectapp.data.TokenData

interface SGOConnectRepository {
    suspend fun createToken(tokenInput: TokenData): Result<TokenID>
}
