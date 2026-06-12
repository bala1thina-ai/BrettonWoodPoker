package com.brettonwood.poker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.brettonwood.poker.databinding.ActivityMainBinding
import com.brettonwood.poker.databinding.DialogNewSessionBinding
import com.brettonwood.poker.ui.adapters.SessionAdapter
import com.brettonwood.poker.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val adapter = SessionAdapter(
            onClick = { session ->
                startActivity(Intent(this, SessionActivity::class.java).apply {
                    putExtra("SESSION_ID", session.id)
                    putExtra("SESSION_NAME", session.name)
                })
            },
            onDelete = { session ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Session")
                    .setMessage("Delete \"${session.name}\"? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteSession(session) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvSessions.layoutManager = LinearLayoutManager(this)
        binding.rvSessions.adapter = adapter

        viewModel.sessions.observe(this) { sessions ->
            adapter.submitList(sessions)
            binding.tvEmpty.visibility = if (sessions.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabNewSession.setOnClickListener { showNewSessionDialog() }
    }

    private fun showNewSessionDialog() {
        val dialogBinding = DialogNewSessionBinding.inflate(layoutInflater)
        dialogBinding.etSessionName.setText(defaultSessionName())
        dialogBinding.etSessionName.selectAll()
        AlertDialog.Builder(this)
            .setTitle("New Session")
            .setView(dialogBinding.root)
            .setPositiveButton("Create") { _, _ ->
                val name = dialogBinding.etSessionName.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.createSession(name) { sessionId ->
                        runOnUiThread {
                            startActivity(Intent(this, SessionActivity::class.java).apply {
                                putExtra("SESSION_ID", sessionId)
                                putExtra("SESSION_NAME", name)
                            })
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
        dialogBinding.etSessionName.requestFocus()
    }

    private fun defaultSessionName(): String {
        val cal = Calendar.getInstance()
        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val suffix = when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
        return "$month $day$suffix Game"
    }
}
