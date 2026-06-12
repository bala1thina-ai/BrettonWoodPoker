package com.brettonwood.poker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.brettonwood.poker.data.dao.BuyInDao
import com.brettonwood.poker.data.dao.PlayerDao
import com.brettonwood.poker.data.dao.SessionDao
import com.brettonwood.poker.data.entities.BuyIn
import com.brettonwood.poker.data.entities.Player
import com.brettonwood.poker.data.entities.Session

@Database(entities = [Session::class, Player::class, BuyIn::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun playerDao(): PlayerDao
    abstract fun buyInDao(): BuyInDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "brettonwood_poker_db")
                    .build().also { INSTANCE = it }
            }
    }
}
