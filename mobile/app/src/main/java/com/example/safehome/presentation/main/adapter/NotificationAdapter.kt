package com.example.safehome.presentation.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.safehome.data.model.NotificationItem
import com.example.safehome.databinding.ItemNotificationBinding
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter : ListAdapter<NotificationItem, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationItem) {
            with(binding){
                titleTextView.text = notification.title
                bodyTextView.text = notification.body
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.ENGLISH)
                    val outputFormat = SimpleDateFormat("HH:mm MMMM dd", Locale.ENGLISH)
                    val date = inputFormat.parse(notification.created_at)
                    dateTextView.text = date?.let { outputFormat.format(it) } ?: notification.created_at
                } catch (e: Exception) {
                    Timber.tag("NotificationViewModel").e("Network error: ${e.message}")
                    dateTextView.text = notification.created_at
                }
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem == newItem
        }
    }
}