package com.example.safehome.presentation.biometric.viewModel

sealed class BiometricState {
    data object Idle : BiometricState()
    data object SearchingFace : BiometricState()
    data object Processing : BiometricState()
    data object EnrollmentSuccess : BiometricState()
    data object VerificationSuccess : BiometricState()
    data object BiometricDisabled : BiometricState()
    data class Error(val message: String) : BiometricState()
}
