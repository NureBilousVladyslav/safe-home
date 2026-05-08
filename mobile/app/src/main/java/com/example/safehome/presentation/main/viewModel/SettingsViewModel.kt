package com.example.safehome.presentation.main.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.safehome.data.repo.BiometricRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val biometricRepository: BiometricRepository
) : ViewModel() {

    private val _isBiometricEnabled = MutableLiveData(false)
    val isBiometricEnabled: LiveData<Boolean> = _isBiometricEnabled

    fun loadBiometricState() {
        _isBiometricEnabled.value = biometricRepository.isBiometricEnabled()
    }

    fun clearBiometricData() {
        try {
            biometricRepository.clearBiometricData()
            _isBiometricEnabled.value = false
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to clear biometric data")
        }
    }
}
