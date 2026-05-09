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
                    biometricRepository.markBiometricSessionVerified()
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
}