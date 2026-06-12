package com.brettonwood.poker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brettonwood.poker.data.PlayerWithTotal
import com.brettonwood.poker.databinding.ItemPlayerBinding

class PlayerAdapter(
    private val onQuickRebuy: (PlayerWithTotal) -> Unit,
    private val onUndoRebuy: (PlayerWithTotal) -> Unit,
    private val onCashOut: (PlayerWithTotal) -> Unit,
    private val onRemove: (PlayerWithTotal) -> Unit
) : ListAdapter<PlayerWithTotal, PlayerAdapter.ViewHolder>(DiffCallback()) {

    private val expandedIds = mutableSetOf<Long>()

    private fun toggle(playerId: Long) {
        if (playerId in expandedIds) expandedIds.remove(playerId) else expandedIds.add(playerId)
        val pos = currentList.indexOfFirst { it.playerId == playerId }
        if (pos != -1) notifyItemChanged(pos)
    }

    inner class ViewHolder(private val binding: ItemPlayerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(player: PlayerWithTotal) {
            val isExpanded = player.playerId in expandedIds

            binding.tvPlayerName.text = player.name
            binding.tvChevron.text = if (isExpanded) "▲" else "▼"
            binding.layoutHeader.setOnClickListener { toggle(player.playerId) }

            // Collapsed summary: net (colored) if settled, total (gold) if still playing
            if (player.hasCashedOut) {
                val net = player.net
                if (net >= 0) {
                    binding.tvCollapsedInfo.text = "+$%.2f".format(net)
                    binding.tvCollapsedInfo.setTextColor(0xFF2ECC71.toInt())
                } else {
                    binding.tvCollapsedInfo.text = "-$%.2f".format(-net)
                    binding.tvCollapsedInfo.setTextColor(0xFFE74C3C.toInt())
                }
            } else {
                binding.tvCollapsedInfo.text = "$%.2f".format(player.totalAmount)
                binding.tvCollapsedInfo.setTextColor(0xFFE2B96F.toInt())
            }

            // Expanded section
            binding.layoutExpanded.visibility = if (isExpanded) View.VISIBLE else View.GONE

            binding.tvPlayerTotal.text = "$%.2f".format(player.totalAmount)
            binding.tvBreakdown.text = buildString {
                append("${player.buyInCount} buy-in${if (player.buyInCount != 1) "s" else ""}")
                if (player.rebuyCount > 0) append(" + ${player.rebuyCount} rebuy${if (player.rebuyCount != 1) "s" else ""}")
            }

            if (player.hasCashedOut) {
                binding.tvNet.visibility = View.VISIBLE
                val net = player.net
                if (net >= 0) {
                    binding.tvNet.text = "Net: +$%.2f".format(net)
                    binding.tvNet.setTextColor(0xFF2ECC71.toInt())
                } else {
                    binding.tvNet.text = "Net: -$%.2f".format(-net)
                    binding.tvNet.setTextColor(0xFFE74C3C.toInt())
                }
            } else {
                binding.tvNet.visibility = View.GONE
            }

            binding.btnPlus.setOnClickListener { onQuickRebuy(player) }
            binding.btnMinus.setOnClickListener { onUndoRebuy(player) }
            binding.btnCashOut.setOnClickListener { onCashOut(player) }
            binding.btnRemove.setOnClickListener { onRemove(player) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<PlayerWithTotal>() {
        override fun areItemsTheSame(oldItem: PlayerWithTotal, newItem: PlayerWithTotal) = oldItem.playerId == newItem.playerId
        override fun areContentsTheSame(oldItem: PlayerWithTotal, newItem: PlayerWithTotal) = oldItem == newItem
    }
}
