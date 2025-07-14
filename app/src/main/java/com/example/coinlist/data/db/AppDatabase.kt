package com.example.coinlist.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.coinlist.data.db.dao.CoinDao
import com.example.coinlist.data.db.entity.CoinEntity

@Database(
    entities = [CoinEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getCoinDao(): CoinDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room
                .databaseBuilder(context, AppDatabase::class.java, "app_db")
                .build()

        }
    }
}