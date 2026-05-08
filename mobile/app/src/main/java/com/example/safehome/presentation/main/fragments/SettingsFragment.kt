package com.example.safehome.presentation.main.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.safehome.R
import com.example.safehome.data.local.PrefKeys
import com.example.safehome.databinding.FragmentSettingsBinding
import com.example.safehome.presentation.biometric.BiometricActivity
import com.example.safehome.presentation.main.viewModel.SettingsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private val settingsViewModel: SettingsViewModel by viewModels()

    private var isUpdatingFaceIdSwitchProgrammatically = false
    private var isUpdatingNotificationSwitchProgrammatically = false

    private val prefs by lazy {
        requireActivity().getSharedPreferences(PrefKeys.PREFS_SETTINGS, Context.MODE_PRIVATE)
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                prefs.edit { putBoolean(PrefKeys.KEY_NOTIFICATIONS, true) }
                showMessage("Notifications are enabled")
            } else {
                isUpdatingNotificationSwitchProgrammatically = true
                binding.switchNotify.isChecked = false
                isUpdatingNotificationSwitchProgrammatically = false

                prefs.edit { putBoolean(PrefKeys.KEY_NOTIFICATIONS, false) }
                showMessage("Permission denied. Notifications turned off.")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                ) {
                    showNotificationSettingsDialog()
                }
            }
        }

    private val faceEnrollmentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val isEnrollmentSuccessful = result.resultCode == Activity.RESULT_OK

            if (isEnrollmentSuccessful) {
                settingsViewModel.loadBiometricState()
                showMessage("Face ID has been enabled")
            } else {
                updateFaceIdSwitch(false)
                showMessage("Face ID setup was cancelled or failed")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeSettingsState()
        initButton()
        settingsViewModel.loadBiometricState()
    }

    private fun observeSettingsState() {
        settingsViewModel.isBiometricEnabled.observe(viewLifecycleOwner) { isEnabled ->
            updateFaceIdSwitch(isEnabled)
        }
    }

    private fun initButton() {
        with(binding) {
            backButton.setOnClickListener {
                findNavController().popBackStack()
            }

            changePasswordConstraintLayout.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_changePasswordFragment)
            }

            switchNotify.isChecked = prefs.getBoolean(PrefKeys.KEY_NOTIFICATIONS, true)
            switchNotify.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdatingNotificationSwitchProgrammatically) {
                    return@setOnCheckedChangeListener
                }

                if (isChecked) {
                    requestNotificationPermission()
                } else {
                    prefs.edit { putBoolean(PrefKeys.KEY_NOTIFICATIONS, false) }
                    showMessage("Notifications are turned off")
                }
            }

            switchFaceId.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdatingFaceIdSwitchProgrammatically) {
                    return@setOnCheckedChangeListener
                }

                if (isChecked) {
                    startFaceEnrollment()
                } else {
                    settingsViewModel.clearBiometricData()
                    showMessage("Face ID has been disabled")
                }
            }
        }
    }

    private fun updateFaceIdSwitch(isEnabled: Boolean) {
        isUpdatingFaceIdSwitchProgrammatically = true
        binding.switchFaceId.isChecked = isEnabled
        isUpdatingFaceIdSwitchProgrammatically = false
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            prefs.edit { putBoolean(PrefKeys.KEY_NOTIFICATIONS, true) }
            showMessage("Notifications are enabled")
            return
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                prefs.edit { putBoolean(PrefKeys.KEY_NOTIFICATIONS, true) }
                showMessage("Notifications are enabled")
            }

            else -> {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showNotificationSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_notification_permission, null)
        val cancelButton = dialogView.findViewById<TextView>(R.id.cancelButton)
        val confirmButton = dialogView.findViewById<TextView>(R.id.confirmButton)

        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .create()
            .apply {
                show()

                cancelButton.setOnClickListener {
                    dismiss()
                }

                confirmButton.setOnClickListener {
                    dismiss()
                    redirectToAppSettings()
                }
            }
    }

    private fun redirectToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }

        startActivity(intent)
    }

    private fun startFaceEnrollment() {
        val intent = Intent(requireContext(), BiometricActivity::class.java).apply {
            putExtra(BiometricActivity.EXTRA_MODE, BiometricActivity.MODE_ENROLLMENT)
        }

        faceEnrollmentLauncher.launch(intent)
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
