package com.brettonwood.poker.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.brettonwood.poker.R
import com.brettonwood.poker.databinding.ActivitySessionBinding
import com.brettonwood.poker.databinding.DialogAddPlayerBinding
import com.brettonwood.poker.databinding.DialogCashoutBinding
import com.brettonwood.poker.data.PlayerWithTotal
import com.brettonwood.poker.ui.adapters.PlayerAdapter
import com.brettonwood.poker.viewmodel.SessionViewModel

class SessionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySessionBinding
    private val viewModel: SessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionId = intent.getLongExtra("SESSION_ID", -1)
        val sessionName = intent.getStringExtra("SESSION_NAME") ?: "Session"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = sessionName

        viewModel.init(sessionId)

        val adapter = PlayerAdapter(
            onQuickRebuy = { viewModel.quickRebuy(it.playerId) },
            onUndoRebuy = { viewModel.undoRebuy(it.playerId) },
            onCashOut = { showCashOutDialog(it) },
            onRemove = { player ->
                AlertDialog.Builder(this)
                    .setTitle("Remove Player")
                    .setMessage("Remove ${player.name} from this session?")
                    .setPositiveButton("Remove") { _, _ -> viewModel.removePlayerById(player.playerId) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvPlayers.layoutManager = LinearLayoutManager(this)
        binding.rvPlayers.adapter = adapter

        viewModel.playersWithTotals.observe(this) { players ->
            adapter.submitList(players)
            val totalIn = players.sumOf { it.totalAmount }
            val totalSettled = players.sumOf { it.cashoutAmount }
            binding.tvTotalPot.text = "Total Pot: $%.2f".format(totalIn)
            binding.tvSummaryTotalIn.text = "$%.2f".format(totalIn)
            binding.tvSummaryRebuys.text = "$%.2f".format(totalSettled)
            binding.tvEmpty.visibility = if (players.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddPlayer.setOnClickListener { showAddPlayerDialog() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_session, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_rename) {
            showRenameDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun showRenameDialog() {
        val padding = (24 * resources.displayMetrics.density).toInt()
        val editText = EditText(this).apply {
            setText(supportActionBar?.title)
            selectAll()
        }
        val container = FrameLayout(this).apply {
            setPadding(padding, 0, padding, 0)
            addView(editText)
        }
        AlertDialog.Builder(this)
            .setTitle("Rename Session")
            .setView(container)
            .setPositiveButton("Rename") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.renameSession(newName)
                    supportActionBar?.title = newName
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
        editText.requestFocus()
    }

    private fun showAddPlayerDialog() {
        val dialogBinding = DialogAddPlayerBinding.inflate(layoutInflater)
        AlertDialog.Builder(this)
            .setTitle("Add Player")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.etPlayerName.text.toString().trim()
                val amount = dialogBinding.etBuyInAmount.text.toString().toDoubleOrNull() ?: 0.0
                if (name.isNotEmpty()) viewModel.addPlayer(name, amount)
            }
            .setNegativeButton("Cancel", null)
            .show()
        dialogBinding.etPlayerName.requestFocus()
    }

    private fun showCashOutDialog(player: PlayerWithTotal) {
        val dialogBinding = DialogCashoutBinding.inflate(layoutInflater)
        dialogBinding.tvCashOutInfo.text = "${player.name}  —  Total in: $%.2f".format(player.totalAmount)
        AlertDialog.Builder(this)
            .setTitle("Cash Out")
            .setView(dialogBinding.root)
            .setPositiveButton("Settle") { _, _ ->
                val chips = dialogBinding.etChipsReturned.text.toString().toDoubleOrNull() ?: 0.0
                viewModel.cashOut(player.playerId, chips)
            }
            .setNegativeButton("Cancel", null)
            .show()
        dialogBinding.etChipsReturned.requestFocus()
    }
}
