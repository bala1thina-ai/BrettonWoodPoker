package com.brettonwood.poker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.brettonwood.poker.data.entities.BuyIn

@Dao
interface BuyInDao {
    @Query("SELECT * FROM buyins WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getBuyInsForSession(sessionId: Long): LiveData<List<BuyIn>>

    @Insert
    suspend fun insertBuyIn(buyIn: BuyIn): Long

    @Delete
    suspend fun deleteBuyIn(buyIn: BuyIn)

    @Query("SELECT * FROM buyins WHERE playerId = :playerId AND type = 'REBUY' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRebuy(playerId: Long): BuyIn?

    @Query("DELETE FROM buyins WHERE playerId = :playerId AND type = 'CASHOUT'")
    suspend fun deleteCashOutsForPlayer(playerId: Long)
}
