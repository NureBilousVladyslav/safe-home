package com.example.safehome.presentation.main.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.safehome.data.model.SensorDto
import com.example.safehome.databinding.ItemSensorBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.safehome.R
import com.google.android.material.button.MaterialButton
import timber.log.Timber

class SensorAdapter(
    private val onArchiveClick: (String, Boolean) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onActiveChange: (String, Boolean, (Boolean) -> Unit) -> Unit,
) : ListAdapter<SensorDto, SensorAdapter.SensorViewHolder>(SensorDiffCallback()) {

    class SensorViewHolder(
        private val binding: ItemSensorBinding,
        private val onArchiveClick: (String, Boolean) -> Unit,
        private val onDeleteClick: (String) -> Unit,
        private val onActiveChange: (String, Boolean, (Boolean) -> Unit) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        private var previousActiveState: Boolean? = null

        fun bind(sensor: SensorDto) {
            val status = when {
                sensor.is_security_breached -> "alarm"
                sensor.is_closed -> "close"
                else -> "open"
            }

            val typeImageName = when (sensor.type) {
                "window" -> "ic_window"
                "door" -> "ic_door"
                else -> "ic_error"
            }

            with(binding) {
                updateStatusUI(status)

                val resTypeImageId = root.context.resources.getIdentifier(
                    typeImageName,
                    "drawable",
                    root.context.packageName
                )

                if (resTypeImageId != 0) {
                    sensorImageView.setImageResource(resTypeImageId)
                } else {
                    Timber.tag("ImageViewError").e("Drawable resource not found: $typeImageName")
                }

                nameTextView.text = sensor.name
                statusTextView.text = status

                propertiesImageView.setOnClickListener {
                    showPropertiesSensorDialog(sensor)
                }
                root.setOnClickListener {
                    showPropertiesSensorDialog(sensor)
                }

                previousActiveState = sensor.is_active
                activitySwitch.isChecked = sensor.is_active
                activitySwitch.isVisible = !sensor.is_archived
                activitySwitch.isEnabled = true

                activitySwitch.setOnCheckedChangeListener { _, isChecked ->
                    Timber.tag("SensorAdapter").d("Switch changed: sensorId=${sensor.sensor_id}, isChecked=$isChecked, previousActiveState=$previousActiveState")
                    if (previousActiveState != isChecked) {
                        activitySwitch.isEnabled = false
                        onActiveChange(sensor.sensor_id, isChecked) { success ->
                            Timber.tag("SensorAdapter").d("onActiveChange callback: sensorId=${sensor.sensor_id}, success=$success")
                            if (success) {
                                previousActiveState = isChecked
                                activitySwitch.isEnabled = true
                            } else {
                                activitySwitch.isChecked = previousActiveState ?: sensor.is_active
                                activitySwitch.isEnabled = true
                            }
                        }
                    } else {
                        Timber.tag("SensorAdapter").w("No change detected: sensorId=${sensor.sensor_id}, isChecked=$isChecked, previousActiveState=$previousActiveState")
                    }
                }

                sensorImageView.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        root.context,
                        if (sensor.is_archived) R.color.grey else R.color.primary
                    )
                )
            }
        }

        private fun updateStatusUI(status: String) {
            val statusImageName = when (status) {
                "open" -> "ic_lock_open"
                "close" -> "ic_lock_close"
                "custom" -> "ic_custom"
                "alert", "alarm" -> "ic_alarm"
                else -> "ic_error"
            }

            with(binding) {
                val resId = root.context.resources.getIdentifier(
                    statusImageName,
                    "drawable",
                    root.context.packageName
                )

                if (resId != 0) {
                    statusImageView.setImageResource(resId)
                } else {
                    Timber.tag("SensorAdapter").e("Drawable resource not found: $statusImageName")
                }

                statusTextView.text = status.replaceFirstChar { it.uppercaseChar() }
            }
        }

        private fun showPropertiesSensorDialog(sensor: SensorDto) {
            val dialogView = LayoutInflater.from(binding.root.context)
                .inflate(R.layout.dialog_sensor_properties, null)
            val titleTextView = dialogView.findViewById<TextView>(R.id.titleTextView)
            val archiveButton = dialogView.findViewById<MaterialButton>(R.id.archiveButton)
            val deleteButton = dialogView.findViewById<MaterialButton>(R.id.deleteButton)
            val paringCode = dialogView.findViewById<TextView>(R.id.paringCodeTextView)
            val cancelButton = dialogView.findViewById<TextView>(R.id.cancelButton)

            titleTextView.text = sensor.name
            archiveButton.text = if (sensor.is_archived) "UnArchived" else "Archived"
            paringCode.text = sensor.short_code

            MaterialAlertDialogBuilder(binding.root.context, R.style.CustomDialogStyle)
                .setView(dialogView)
                .create()
                .apply {
                    show()
                    archiveButton.setOnClickListener {
                        onArchiveClick(sensor.sensor_id, sensor.is_archived)
                        dismiss()
                    }
                    deleteButton.setOnClickListener {
                        onDeleteClick(sensor.sensor_id)
                        dismiss()
                    }
                    cancelButton.setOnClickListener {
                        dismiss()
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val binding = ItemSensorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SensorViewHolder(binding, onArchiveClick, onDeleteClick, onActiveChange)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class SensorDiffCallback : DiffUtil.ItemCallback<SensorDto>() {
        override fun areItemsTheSame(oldItem: SensorDto, newItem: SensorDto): Boolean {
            return oldItem.sensor_id == newItem.sensor_id
        }

        override fun areContentsTheSame(oldItem: SensorDto, newItem: SensorDto): Boolean {
            return oldItem == newItem
        }
    }
}