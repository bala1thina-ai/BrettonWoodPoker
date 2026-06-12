package com.brettonwood.poker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.brettonwood.poker.data.entities.Player

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE sessionId = :sessionId ORDER BY name ASC")
    fun getPlayersForSession(sessionId: Long): LiveData<List<Player>>

    @Insert
    suspend fun insertPlayer(player: Player): Long

    @Delete
    suspend fun deletePlayer(player: Player)

    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerById(playerId: Long): Player?
}
