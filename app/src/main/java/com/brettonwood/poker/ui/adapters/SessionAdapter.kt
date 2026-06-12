package com.brettonwood.poker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brettonwood.poker.data.entities.Session
import com.brettonwood.poker.databinding.ItemSessionBinding
import java.text.SimpleDateFormat
import java.util.*

class SessionAdapter(
    private val onClick: (Session) -> Unit,
    private val onDelete: (Session) -> Unit
) : ListAdapter<Session, SessionAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(session: Session) {
            binding.tvSessionName.text = session.name
            binding.tvSessionDate.text = SimpleDateFormat("EEE, MMM dd yyyy  •  h:mm a", Locale.getDefault()).format(Date(session.date))
            binding.root.setOnClickListener { onClick(session) }
            binding.btnDelete.setOnClickListener { onDelete(session) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Session>() {
        override fun areItemsTheSame(oldItem: Session, newItem: Session) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Session, newItem: Session) = oldItem == newItem
    }
}
