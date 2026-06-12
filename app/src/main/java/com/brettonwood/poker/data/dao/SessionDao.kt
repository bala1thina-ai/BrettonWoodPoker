package com.brettonwood.poker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.brettonwood.poker.data.entities.Session

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getAllSessions(): LiveData<List<Session>>

    @Insert
    suspend fun insertSession(session: Session): Long

    @Delete
    suspend fun deleteSession(session: Session)

    @Query("UPDATE sessions SET name = :name WHERE id = :id")
    suspend fun updateSessionName(id: Long, name: String)
}
