package com.example.safehome.presentation.main.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safehome.R
import com.example.safehome.data.model.SensorDto
import com.example.safehome.databinding.FragmentSensorBinding
import com.example.safehome.presentation.main.adapter.SensorAdapter
import com.example.safehome.presentation.main.viewModel.HomesViewModel
import com.example.safehome.presentation.main.viewModel.SensorViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SensorFragment : Fragment() {
    private val sensorViewModel: SensorViewModel by viewModels()
    private val homesViewModel: HomesViewModel by activityViewModels()
    private lateinit var binding: FragmentSensorBinding
    private lateinit var sensorAdapter: SensorAdapter
    private lateinit var homeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeId = arguments?.getString("home_id") ?: run {
            Timber.tag("SensorFragment").e("No home_id provided")
            findNavController().popBackStack(R.id.navigation_homes, false)
            return
        }
        sensorViewModel.setContext(requireContext())
        sensorViewModel.setHomeId(homeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSensorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeSensorsState()
        observeHomeState()
        observeErrorMessage()
        setupListeners()
    }

    private fun observeHomeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homesViewModel.homesState.collect { homes ->
                    val home = homes.find { it.home_id == homeId }
                    home?.let {
                        val initialStatus = it.default_mode_name
                        setupHomeSwitch(initialStatus)
                        updateStatusUI(initialStatus)

                        binding.apply {
                            homeNameTextView.text = it.name
                            homeAddressTextView.text = it.address

                            homeArmedSwitch.isVisible = !it.is_archived
                            homeImageView.backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    if (it.is_archived) R.color.grey else R.color.onPrimaryVariant
                                )
                            )
                            ImageViewCompat.setImageTintList(
                                homeImageView,
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        if (it.is_archived) R.color.white else R.color.black
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeSensorsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sensorViewModel.sensorsState.collect { newSensors ->
                    val currentSensors = sensorAdapter.currentList
                    if (currentSensors != newSensors) {
                        sensorAdapter.submitList(newSensors)
                    }
                    val effectiveStatus = determineEffectiveStatus(newSensors)
                    updateStatusUI(effectiveStatus)
                    setupHomeSwitch(effectiveStatus)

                    binding.emptyTextView.visibility =
                        if (newSensors.isEmpty())
                            View.VISIBLE
                        else
                            View.GONE
                }
            }
        }
    }

    private fun observeErrorMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sensorViewModel.errorMessage.collect { errorMessage ->
                    errorMessage?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            backButton.setOnClickListener {
                findNavController().popBackStack(R.id.navigation_homes, false)
            }
            addSensorButton.setOnClickListener {
                showAddSensorDialog()
            }
        }
    }

    private fun setupHomeSwitch(status: String) {
        binding.apply {
            when (status) {
                "armed" -> {
                    homeArmedSwitch.isEnabled = true
                    homeArmedSwitch.isChecked = true
                }
                "disarmed" -> {
                    homeArmedSwitch.isEnabled = true
                    homeArmedSwitch.isChecked = false
                }
                "custom", "alert", "alarm" -> {
                    homeArmedSwitch.isEnabled = false
                    homeArmedSwitch.isChecked = true
                }
                else -> {
                    homeArmedSwitch.isVisible = false
                }
            }

            homeArmedSwitch.setOnSwitchEnabledListener { isChecked ->
                if (isChecked) {
                    updateStatusUI("armed")
                    updateAllSensorsActiveState(true)
                    viewLifecycleOwner.lifecycleScope.launch {
                        homesViewModel.armedHome(homeId)
                    }
                }
            }

            homeArmedSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (homeArmedSwitch.isEnabled) {
                    val newStatus = if (isChecked) "armed" else "disarmed"
                    Timber.tag("SensorFragment").d("SwitchCompat state changed to: $isChecked, new status: $newStatus")
                    updateStatusUI(newStatus)
                    updateAllSensorsActiveState(isChecked)
                    viewLifecycleOwner.lifecycleScope.launch {
                        if (isChecked) {
                            homesViewModel.armedHome(homeId)
                        } else {
                            homesViewModel.disarmedHome(homeId)
                        }
                    }
                }
            }
        }
    }

    private fun updateStatusUI(status: String) {
        binding.apply {
            val statusImageName = when (status) {
                "armed" -> "ic_lock_close"
                "disarmed" -> "ic_lock_open"
                "custom" -> "ic_custom"
                "alert", "alarm" -> "ic_alarm"
                else -> "ic_error"
            }

            val resId = resources.getIdentifier(statusImageName, "drawable", requireContext().packageName)
            if (resId != 0) {
                statusImageView.setImageResource(resId)
            } else {
                Timber.tag("SensorFragment").e("Drawable resource not found: $statusImageName")
            }

            statusTextView.text =
                if (status == "alert")
                    "Alarm"
                else
                    status.replaceFirstChar { it.uppercaseChar() }
        }
    }

    private fun determineEffectiveStatus(sensors: List<SensorDto>): String {
        if (sensors.isEmpty()) {
            return homesViewModel.homesState.value.find { it.home_id == homeId }?.default_mode_name ?: "disarmed"
        }

        val hasSecurityBreach = sensors.any { it.is_security_breached }
        val allActive = sensors.all { it.is_active && !it.is_security_breached }
        val allInactive = sensors.all { !it.is_active && !it.is_security_breached }

        return when {
            hasSecurityBreach -> "alarm"
            allActive -> "armed"
            allInactive -> "disarmed"
            else -> "custom"
        }
    }

    private fun updateAllSensorsActiveState(isActive: Boolean) {
        val currentSensors = sensorViewModel.sensorsState.value
        if (currentSensors.isEmpty()) return

        val updatedSensors = currentSensors.map { sensor ->
            sensor.copy(is_active = isActive)
        }
        sensorViewModel.updateSensorsState(updatedSensors)
        sensorAdapter.submitList(updatedSensors)
    }

    private fun setupRecyclerView() {
        sensorAdapter = SensorAdapter(
            onArchiveClick = { sensorId, isArchived ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (isArchived) sensorViewModel.unArchiveSensor(sensorId)
                    else sensorViewModel.archiveSensor(sensorId)
                }
            },
            onDeleteClick = { sensorId ->
                viewLifecycleOwner.lifecycleScope.launch {
                    sensorViewModel.deleteSensor(sensorId)
                }
            },
            onActiveChange = { sensorId, isActive, callback ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val success = sensorViewModel.setActiveSensor(sensorId, isActive)
                    callback(success)
                    if (!success) {
                        val currentHomeStatus = homesViewModel.homesState.value.find { it.home_id == homeId }?.default_mode_name ?: "disarmed"
                        val revertToActive = currentHomeStatus == "armed"
                        val updatedSensors = sensorViewModel.sensorsState.value.map {
                            if (it.sensor_id == sensorId) it.copy(is_active = revertToActive) else it
                        }
                        sensorViewModel.updateSensorsState(updatedSensors)
                        sensorAdapter.submitList(updatedSensors)
                        Toast.makeText(context, "Failed to update sensor status", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        binding.sensorRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sensorAdapter
        }
    }

    private fun toggleUnitButton(
        clickedButton: MaterialButton,
        doorButton: MaterialButton,
        windowButton: MaterialButton,
    ): String {
        val buttons = listOf(doorButton, windowButton)

        clickedButton.isEnabled = false
        clickedButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary)

        buttons.filter { it != clickedButton }.forEach { button ->
            button.isEnabled = true
            button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.onPrimaryVariant)
        }

        return when (clickedButton.id) {
            R.id.doorButton -> "door"
            R.id.windowButton -> "window"
            else -> "error"
        }
    }

    private fun showAddSensorDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_sensor, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val doorButton = dialogView.findViewById<MaterialButton>(R.id.doorButton)
        val windowButton = dialogView.findViewById<MaterialButton>(R.id.windowButton)
        val cancelButton = dialogView.findViewById<TextView>(R.id.cancelButton)
        val addButton = dialogView.findViewById<TextView>(R.id.saveButton)

        var selectedUnit: String = toggleUnitButton(doorButton, doorButton, windowButton)

        doorButton.setOnClickListener {
            selectedUnit = toggleUnitButton(doorButton, doorButton, windowButton)
        }
        windowButton.setOnClickListener {
            selectedUnit = toggleUnitButton(windowButton, doorButton, windowButton)
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .create()
            .apply {
                show()
                cancelButton.setOnClickListener {
                    dismiss()
                }
                addButton.setOnClickListener {
                    val name = nameEditText.text.toString()
                    sensorViewModel.addSensor(homeId, name, selectedUnit)
                    dismiss()
                }
            }
    }
}