package com.brettonwood.poker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "buyins",
    foreignKeys = [ForeignKey(
        entity = Player::class,
        parentColumns = ["id"],
        childColumns = ["playerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("playerId")]
)
data class BuyIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerId: Long,
    val sessionId: Long,
    val amount: Double,
    val type: String,
    val timestamp: Long = System.currentTimeMillis()
)
