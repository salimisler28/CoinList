package com.example.coinlist.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coinlist.data.db.entity.CoinEntity

@Dao
interface CoinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<CoinEntity>)

    @Query("SELECT * FROM coins")
    suspend fun getAll(): List<CoinEntity>
}