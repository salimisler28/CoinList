package com.example.coinlist.data.network

import com.example.coinlist.data.network.dtos.CoinDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface CoinService {
    @Headers(
        "Accept: application/json",
        "x-cg-demo-api-key: CG-2vvZUCbVyJ96EMo3PEMzsDk1"
    )
    @GET("coins/markets")
    suspend fun getAll(@Query("vs_currency") vsCurrency: String): Response<List<CoinDto>>
}

class CoinApi {
    private val service: CoinService = client.create(CoinService::class.java)

    suspend fun getAllMarkets(vsCurrency: String): Response<List<CoinDto>> {
        return service.getAll(vsCurrency)
    }
}