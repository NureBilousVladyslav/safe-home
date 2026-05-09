package com.example.safehome.presentation.main.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safehome.data.api.HomeApi
import com.example.safehome.data.model.AddHomeRequest
import com.example.safehome.data.model.HomeDto
import com.example.safehome.data.model.ErrorResponse
import com.example.safehome.data.repo.BiometricRepository
import com.example.safehome.data.repo.TokenRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class HomesViewModel @Inject constructor(
    private var tokenRepository: TokenRepository,
    private val homeApi: HomeApi,
    private val biometricRepository: BiometricRepository
) : ViewModel() {
    private val _homesState = MutableStateFlow<List<HomeDto>>(emptyList())
    val homesState: StateFlow<List<HomeDto>> = _homesState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _shouldShowFaceIdPrompt = MutableLiveData<Boolean>()
    val shouldShowFaceIdPrompt: LiveData<Boolean> = _shouldShowFaceIdPrompt

    private var refreshJob: Job? = null

    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                loadHomes()
                delay(3000)
            }
        }
    }

    fun isBiometricSessionValid(): Boolean {
        return biometricRepository.isBiometricSessionValid()
    }

    fun checkFaceIdSuggestionPrompt() {
        val shouldShowPrompt =
            !biometricRepository.isBiometricEnabled() &&
                    !biometricRepository.wasFaceIdPromptShown()

        _shouldShowFaceIdPrompt.value = shouldShowPrompt
    }

    fun markFaceIdSuggestionPromptShown() {
        biometricRepository.markFaceIdPromptShown()
        _shouldShowFaceIdPrompt.value = false
    }

    fun loadHomes() {
        viewModelScope.launch {
            try {
                val token = tokenRepository.getToken()
                val response = homeApi.getHomes(token)

                if (response.isSuccessful) {
                    _homesState.value = response.body()?.homes ?: emptyList()
                    _errorMessage.value = null
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseErrorMessage(errorBody)
                    _homesState.value = emptyList()
                    _errorMessage.value = errorMessage
                    Timber.tag("HomeViewModel").e(errorMessage ?: "Unknown error")
                }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _errorMessage.value = errorMessage
                Timber.tag("HomeViewModel").e(errorMessage)
            }
        }
    }

    fun addHome(name: String, address: String) {
        viewModelScope.launch {
            try {
                val token = tokenRepository.getToken()
                val request = AddHomeRequest(name, address)
                val response = homeApi.addHome(token, request)

                if (response.isSuccessful) {
                    loadHomes()
                    _errorMessage.value = null
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseErrorMessage(errorBody)
                    _errorMessage.value = errorMessage
                    Timber.tag("HomeViewModel").e(errorMessage ?: "Unknown error")
                }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _errorMessage.value = errorMessage
                Timber.tag("HomeViewModel").e(errorMessage)
            }
        }
    }

    suspend fun deleteHome(homeId: String) {
        try {
            val token = tokenRepository.getToken()
            val response = homeApi.deleteHome(token, homeId)
            if (response.isSuccessful) {
                loadHomes()
                _errorMessage.value = null
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody)
                _errorMessage.value = errorMessage
                Timber.tag("HomeViewModel").e(errorMessage ?: "Unknown error")
            }
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            _errorMessage.value = errorMessage
            Timber.tag("HomeViewModel").e(errorMessage)
        }
    }

    suspend fun archiveHome(homeId: String) {
        try {
            val token = tokenRepository.getToken()
            val response = homeApi.archiveHome(token, homeId)
            if (response.isSuccessful) {
                loadHomes()
                _errorMessage.value = null
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody)
                _errorMessage.value = errorMessage
                Timber.tag("HomeViewModel").e(errorMessage ?: "Unknown error")
            }
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            _errorMessage.value = errorMessage
            Timber.tag("HomeViewModel").e(errorMessage)
        }
    }

    suspend fun unArchiveHome(homeId: String) {
        try {
            val token = tokenRepository.getToken()
            val response = homeApi.unArchiveHome(token, homeId)
            if (response.isSuccessful) {
                loadHomes()
                _errorMessage.value = null
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody)
                _errorMessage.value = errorMessage
                Timber.tag("HomeViewModel").e(errorMessage ?: "Unknown error")
            }
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            _errorMessage.value = errorMessage
            Timber.tag("HomeViewModel").e(errorMessage)
        }
    }

    suspend fun armedHome(homeId: String) {
        try {
            val token = tokenRepository.getToken()
            val response = homeApi.armedHome(token, homeId)
            if (response.isSuccessful) {
                loadHomes()
                _errorMessage.value = null
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody)
                _errorMessage.value = errorMessage
                Timber.tag("HomeViewModel").e(errorMessage ?: "Unknown error")
            }
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            _errorMessage.value = errorMessage
            Timber.tag("HomeViewModel").e(errorMessage)
        }
    }

    suspend fun disarmedHome(homeId: String) {
        try {
            val token = tokenRepository.getToken()
            val response = homeApi.disarmedHome(token, homeId)
            if (response.isSuccessful) {
                loadHomes()
                _errorMessage.value = null
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody)
                _errorMessage.value = errorMessage
                Timber.tag("HomeViewModel").e(errorMessage ?: "Unknown error")
            }
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            _errorMessage.value = errorMessage
            Timber.tag("HomeViewModel").e(errorMessage)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        return try {
            errorBody?.let {
                val errorResponse = Gson().fromJson(it, ErrorResponse::class.java)
                errorResponse.error
            }
        } catch (e: JsonSyntaxException) {
            "Unknown error: ${e.message}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}