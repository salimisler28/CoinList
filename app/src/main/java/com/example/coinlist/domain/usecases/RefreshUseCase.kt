package com.example.coinlist.domain.usecases

import com.example.coinlist.data.network.CoinApi
import com.example.coinlist.domain.models.CoinModel

class RefreshUseCase constructor(
    private val api: CoinApi
) {
    suspend operator fun invoke(): Result<List<CoinModel>> {
        val res = api.getAllMarkets(vsCurrency = "USD")

        if (res.isSuccessful) {
            val body = res.body()

            return if (body.isNullOrEmpty()) {
                Result.failure(Exception("body is null"))
            } else {
                Result.success(body.map { CoinModel.fromDto(it) })
            }
        } else {
            val error = res.errorBody()
            return Result.failure(Exception(error?.string()))
        }
    }
}