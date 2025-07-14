package com.example.coinlist.domain.models

import com.example.coinlist.data.db.entity.CoinEntity
import com.example.coinlist.data.network.dtos.CoinDto

data class CoinModel(
    val id: String,
    val name: String,
    val imageUrl: String,
    val symbol: String,
    val price: Double,
) {
    companion object {
        fun fromDto(coinDto: CoinDto): CoinModel {
            return CoinModel(
                id = coinDto.id,
                name = coinDto.name,
                imageUrl = coinDto.image,
                symbol = coinDto.symbol,
                price = coinDto.currentPrice
            )
        }

        fun fromEntity(coinEntity: CoinEntity): CoinModel {
            return CoinModel(
                id = coinEntity.id,
                name = coinEntity.name,
                imageUrl = coinEntity.image,
                symbol = coinEntity.symbol,
                price = coinEntity.currentPrice
            )
        }
    }
}
