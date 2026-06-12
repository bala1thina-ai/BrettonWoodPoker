package com.brettonwood.poker.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.brettonwood.poker.data.AppDatabase
import com.brettonwood.poker.data.PlayerWithTotal
import com.brettonwood.poker.data.entities.BuyIn
import com.brettonwood.poker.data.entities.Player
import kotlinx.coroutines.launch

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val playerDao = db.playerDao()
    private val buyInDao = db.buyInDao()
    private val sessionDao = db.sessionDao()

    private var sessionId: Long = -1L

    private lateinit var players: LiveData<List<Player>>
    private lateinit var sessionBuyIns: LiveData<List<BuyIn>>

    private val _playersWithTotals = MediatorLiveData<List<PlayerWithTotal>>()
    val playersWithTotals: LiveData<List<PlayerWithTotal>> = _playersWithTotals

    fun init(sessionId: Long) {
        if (this.sessionId != -1L) return
        this.sessionId = sessionId
        players = playerDao.getPlayersForSession(sessionId)
        sessionBuyIns = buyInDao.getBuyInsForSession(sessionId)

        var latestPlayers: List<Player> = emptyList()
        var latestBuyIns: List<BuyIn> = emptyList()

        val update: () -> Unit = {
            _playersWithTotals.value = latestPlayers.map { player ->
                val playerBuyIns = latestBuyIns.filter { it.playerId == player.id }
                PlayerWithTotal(
                    playerId = player.id,
                    name = player.name,
                    totalAmount = playerBuyIns.filter { it.type != "CASHOUT" }.sumOf { it.amount },
                    buyInCount = playerBuyIns.count { it.type == "BUYIN" },
                    rebuyCount = playerBuyIns.count { it.type == "REBUY" },
                    rebuyTotal = playerBuyIns.filter { it.type == "REBUY" }.sumOf { it.amount },
                    cashoutAmount = playerBuyIns.filter { it.type == "CASHOUT" }.sumOf { it.amount }
                )
            }.sortedWith(
                // Settled players first (sorted by net desc), then unsettled (sorted by totalAmount desc)
                compareByDescending<PlayerWithTotal> { it.hasCashedOut }
                    .thenByDescending { if (it.hasCashedOut) it.net else it.totalAmount }
            )
        }

        _playersWithTotals.addSource(players) { p -> latestPlayers = p; update() }
        _playersWithTotals.addSource(sessionBuyIns) { b -> latestBuyIns = b; update() }
    }

    fun addPlayer(name: String, initialBuyIn: Double) {
        viewModelScope.launch {
            val playerId = playerDao.insertPlayer(Player(sessionId = sessionId, name = name))
            buyInDao.insertBuyIn(BuyIn(playerId = playerId, sessionId = sessionId, amount = initialBuyIn, type = "BUYIN"))
        }
    }

    fun quickRebuy(playerId: Long) {
        viewModelScope.launch {
            buyInDao.insertBuyIn(BuyIn(playerId = playerId, sessionId = sessionId, amount = 30.0, type = "REBUY"))
        }
    }

    fun undoRebuy(playerId: Long) {
        viewModelScope.launch {
            buyInDao.getLastRebuy(playerId)?.let { buyInDao.deleteBuyIn(it) }
        }
    }

    fun cashOut(playerId: Long, amount: Double) {
        viewModelScope.launch {
            buyInDao.deleteCashOutsForPlayer(playerId)
            buyInDao.insertBuyIn(BuyIn(playerId = playerId, sessionId = sessionId, amount = amount, type = "CASHOUT"))
        }
    }

    fun renameSession(name: String) {
        viewModelScope.launch {
            sessionDao.updateSessionName(sessionId, name)
        }
    }

    fun removePlayerById(playerId: Long) {
        viewModelScope.launch {
            playerDao.getPlayerById(playerId)?.let { playerDao.deletePlayer(it) }
        }
    }
}
