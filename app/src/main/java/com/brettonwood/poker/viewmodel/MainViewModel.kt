package com.brettonwood.poker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.brettonwood.poker.data.AppDatabase
import com.brettonwood.poker.data.entities.Session
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val sessionDao = db.sessionDao()

    val sessions: LiveData<List<Session>> = sessionDao.getAllSessions()

    fun createSession(name: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = sessionDao.insertSession(Session(name = name))
            onCreated(id)
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch { sessionDao.deleteSession(session) }
    }
}
