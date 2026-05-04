package com.example.safehome.presentation.biometric.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safehome.data.repo.BiometricRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BiometricViewModel @Inject constructor(
    private val biometricRepository: BiometricRepository
) : ViewModel() {

    private val _state = MutableLiveData<BiometricState>(BiometricState.Idle)
    val state: LiveData<BiometricState> = _state

    fun enrollFace(embedding: FloatArray) {
        viewModelScope.launch {
            _state.value = BiometricState.Processing

            try {
                biometricRepository.saveFaceTemplate(embedding)
                _state.value = BiometricState.EnrollmentSuccess
            } catch (exception: Exception) {
                Timber.e(exception, "Failed to save biometric data")
                _state.value = BiometricState.Error(
                    exception.localizedMessage ?: "Failed to save biometric data"
                )
            }
        }
    }

    fun verifyFace(embedding: FloatArray) {
        viewModelScope.launch {
            _state.value = BiometricState.Processing

            try {
                val isVerified = biometricRepository.verifyFace(embedding)

                _state.value = if (isVerified) {
                    BiometricState.VerificationSuccess
                } else {
                    BiometricState.Error("Face was not recognized")
                }
            } catch (exception: Exception) {
                Timber.e(exception, "Face verification failed")
                _state.value = BiometricState.Error(
                    exception.localizedMessage ?: "Face verification failed"
                )
            }
        }
    }

    fun clearBiometricData() {
        viewModelScope.launch {
            try {
                biometricRepository.clearBiometricData()
                _state.value = BiometricState.BiometricDisabled
            } catch (exception: Exception) {
                Timber.e(exception, "Failed to disable biometric authentication")
                _state.value = BiometricState.Error(
                    exception.localizedMessage ?: "Failed to disable biometric authentication"
                )
            }
        }
    }
}

sealed class BiometricState {
    data object Idle : BiometricState()
    data object SearchingFace : BiometricState()
    data object Processing : BiometricState()
    data object EnrollmentSuccess : BiometricState()
    data object VerificationSuccess : BiometricState()
    data object BiometricDisabled : BiometricState()
    data class Error(val message: String) : BiometricState()
}
