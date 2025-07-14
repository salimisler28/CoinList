package com.example.coinlist.domain.usecases

import app.cash.turbine.test
import com.example.coinlist.data.db.dao.CoinDao
import com.example.coinlist.data.db.entity.CoinEntity
import com.example.coinlist.data.network.CoinApi
import com.example.coinlist.data.network.dtos.CoinDto
import com.example.coinlist.domain.errorhandling.AppError
import com.example.coinlist.domain.models.CoinModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class InitUseCaseTest {
    private val mockCoinApi: CoinApi = mockk()
    private val mockCoinDao: CoinDao = mockk(relaxed = true)
    private lateinit var initUseCase: InitUseCase

    val dummyCoinDtoList: List<CoinDto> = listOf(
        CoinDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            image = "https://dummyimage.com/64x64/000/fff.png&text=BTC",
            currentPrice = 30000.0,
            marketCap = 600000000000,
            marketCapRank = 1,
            fullyDilutedValuation = 630000000000,
            totalVolume = 25000000000,
            high24h = 31000.0,
            low24h = 29500.0,
            priceChange24h = -500.0,
            priceChangePercentage24h = -1.64,
            marketCapChange24h = -10000000000.0,
            marketCapChangePercentage24h = -1.64,
            circulatingSupply = 19000000.0,
            totalSupply = 21000000.0,
            maxSupply = 21000000.0,
            ath = 69000.0,
            athChangePercentage = -56.52,
            athDate = "2021-11-10T14:24:00Z",
            atl = 67.81,
            atlChangePercentage = 44230.65,
            atlDate = "2013-07-06T00:00:00Z",
            lastUpdated = "2025-07-14T10:00:00Z"
        )
    )

    val dummyCoinEntityList: List<CoinEntity> = listOf(
        CoinEntity(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            image = "https://dummyimage.com/64x64/000/fff.png&text=BTC",
            currentPrice = 30000.0,
            marketCap = 600000000000,
            marketCapRank = 1,
            fullyDilutedValuation = 630000000000,
            totalVolume = 25000000000,
            high24h = 31000.0,
            low24h = 29500.0,
            priceChange24h = -500.0,
            priceChangePercentage24h = -1.64,
            marketCapChange24h = -10000000000.0,
            marketCapChangePercentage24h = -1.64,
            circulatingSupply = 19000000.0,
            totalSupply = 21000000.0,
            maxSupply = 21000000.0,
            ath = 69000.0,
            athChangePercentage = -56.52,
            athDate = "2021-11-10T14:24:00Z",
            atl = 67.81,
            atlChangePercentage = 44230.65,
            atlDate = "2013-07-06T00:00:00Z",
            lastUpdated = "2025-07-14T10:00:00Z"
        )
    )

    @Before
    fun before() {
        MockKAnnotations.init(this)
        initUseCase = InitUseCase(
            coinApi = mockCoinApi,
            coinDao = mockCoinDao
        )
    }

    @Test
    fun allDone() {
        runBlocking {
            coEvery {
                mockCoinApi.getAllMarkets(vsCurrency = "USD")
            } returns Response.success(dummyCoinDtoList)

            coEvery { mockCoinDao.getAll() } returns dummyCoinEntityList

            val result = initUseCase.invoke()

            result.test {
                assertEquals(InitState.Loading, awaitItem())
                assertEquals(InitState.DbDoneApiWaiting(data = dummyCoinEntityList.map {
                    CoinModel.fromEntity(
                        it
                    )
                }), awaitItem())
                assertEquals(
                    InitState.AllDone(
                        data = dummyCoinDtoList.map { CoinModel.fromDto(it) }
                    ), awaitItem()
                )
                awaitComplete()
            }
        }
    }

    @Test
    fun dbDoneApiError() {
        runBlocking {
            coEvery {
                mockCoinApi.getAllMarkets(vsCurrency = "USD")
            } returns Response.error(404, "some error".toResponseBody())

            coEvery { mockCoinDao.getAll() } returns dummyCoinEntityList

            val result = initUseCase.invoke()

            result.test {
                assertEquals(InitState.Loading, awaitItem())
                assertEquals(InitState.DbDoneApiWaiting(data = dummyCoinEntityList.map {
                    CoinModel.fromEntity(
                        it
                    )
                }), awaitItem())
                assertEquals(
                    InitState.DbDoneApiError(
                        data = dummyCoinEntityList.map { CoinModel.fromEntity(it) },
                        exception = AppError.NetworkError()
                    ), awaitItem()
                )
                awaitComplete()
            }
        }
    }
}