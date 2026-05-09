package com.example.safehome.presentation.biometric.utils

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.safehome.presentation.biometric.BiometricActivity

fun Fragment.registerBiometricVerificationLauncher(
    isSessionValid: () -> Boolean,
    onVerificationFailed: () -> Unit = {
        Toast.makeText(
            requireContext(),
            "Biometric verification was cancelled or failed",
            Toast.LENGTH_SHORT
        ).show()
    }
):(action: () -> Unit) -> Unit {
    var pendingAction: (() -> Unit)? = null

    val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val action = pendingAction
        pendingAction = null

        if (result.resultCode == Activity.RESULT_OK && action != null) {
            action()
        } else {
            onVerificationFailed()
        }
    }

    return { action ->
        if (isSessionValid()) {
            action()
        } else {
            pendingAction = action

            val intent = Intent(requireContext(), BiometricActivity::class.java).apply {
                putExtra(BiometricActivity.EXTRA_MODE, BiometricActivity.MODE_RECOGNITION)
            }

            launcher.launch(intent)
        }
    }
}
