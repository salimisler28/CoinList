package com.example.coinlist.domain.usecases

import com.example.coinlist.data.db.dao.CoinDao
import com.example.coinlist.data.db.entity.CoinEntity
import com.example.coinlist.data.network.CoinApi
import com.example.coinlist.domain.errorhandling.AppError
import com.example.coinlist.domain.models.CoinModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class InitState {
    data object Loading : InitState()
    data class Error(val appError: AppError) : InitState()
    data class DbDoneApiWaiting(val data: List<CoinModel>) : InitState()
    data class DbDoneApiError(val data: List<CoinModel>, val exception: AppError?) : InitState()
    data class AllDone(val data: List<CoinModel>) : InitState()
}

class InitUseCase constructor(
    private val coinApi: CoinApi,
    private val coinDao: CoinDao
) {
    operator fun invoke(): Flow<InitState> {
        return flow {
            try {
                emit(InitState.Loading)
                val dbData = coinDao.getAll().map {
                    CoinModel.fromEntity(it)
                }
                emit(InitState.DbDoneApiWaiting(dbData))

                val res = coinApi.getAllMarkets(vsCurrency = "USD")
                if (res.isSuccessful) {
                    val body = res.body()
                    if (body.isNullOrEmpty()) {
                        emit(InitState.DbDoneApiError(dbData, AppError.NetworkError()))
                    } else {
                        coinDao.insertAll(
                            body.map { dto ->
                                CoinEntity(
                                    id = dto.id,
                                    symbol = dto.symbol,
                                    name = dto.name,
                                    image = dto.image,
                                    currentPrice = dto.currentPrice,
                                    marketCap = dto.marketCap,
                                    marketCapRank = dto.marketCapRank,
                                    fullyDilutedValuation = dto.fullyDilutedValuation,
                                    totalVolume = dto.totalVolume,
                                    high24h = dto.high24h,
                                    low24h = dto.low24h,
                                    priceChange24h = dto.priceChange24h,
                                    priceChangePercentage24h = dto.priceChangePercentage24h,
                                    marketCapChange24h = dto.marketCapChange24h,
                                    marketCapChangePercentage24h = dto.marketCapChangePercentage24h,
                                    circulatingSupply = dto.circulatingSupply,
                                    totalSupply = dto.totalSupply,
                                    maxSupply = dto.maxSupply,
                                    ath = dto.ath,
                                    athChangePercentage = dto.athChangePercentage,
                                    athDate = dto.athDate,
                                    atl = dto.atl,
                                    atlChangePercentage = dto.atlChangePercentage,
                                    atlDate = dto.atlDate,
                                    lastUpdated = dto.lastUpdated
                                )
                            }
                        )
                        emit(InitState.AllDone(body.map { CoinModel.fromDto(it) }))
                    }
                } else {
                    emit(InitState.DbDoneApiError(dbData, AppError.NetworkError()))
                }
            } catch (e: Exception) {
                emit(InitState.Error(AppError.UnknownError()))
            }
        }
    }
}