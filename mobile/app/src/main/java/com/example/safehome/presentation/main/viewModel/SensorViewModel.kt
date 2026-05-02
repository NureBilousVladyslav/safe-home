package com.example.safehome.presentation.main.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safehome.data.api.SensorApi
import com.example.safehome.data.model.ActiveSensorRequest
import com.example.safehome.data.model.AddSensorRequest
import com.example.safehome.data.model.ErrorResponse
import com.example.safehome.data.model.SensorDto
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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SensorViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val sensorApi: SensorApi
) : ViewModel() {
    private val _sensorsState = MutableStateFlow<List<SensorDto>>(emptyList())
    val sensorsState: StateFlow<List<SensorDto>> = _sensorsState.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var homeId: String? = null
    private var refreshJob: Job? = null
    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context.applicationContext
    }

    fun setHomeId(homeId: String) {
        this.homeId = homeId
        startAutoRefresh()
        loadSensors()
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                loadSensors()
                delay(3000)
            }
        }
    }

    fun loadSensors() {
        viewModelScope.launch {
            try {
                val token = tokenRepository.getToken()
                val response = sensorApi.getSensors(token, homeId!!)

                if (response.isSuccessful) {
                    val newSensors = response.body()?.sensors ?: emptyList()
                    if (_sensorsState.value != newSensors) {
                        _sensorsState.value = newSensors
                    }
                    _errorMessage.value = null
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseErrorMessage(errorBody)
                    _sensorsState.value = emptyList()
                    _errorMessage.value = errorMessage
                    Timber.tag("SensorViewModel").e(errorMessage ?: "Unknown error")
                }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _errorMessage.value = errorMessage
                Timber.tag("SensorViewModel").e(errorMessage)
            }
        }
    }

    fun updateSensorsState(updatedSensors: List<SensorDto>) {
        _sensorsState.value = updatedSensors
    }

    fun addSensor(homeId: String, name: String, type: String) {
        viewModelScope.launch {
            try {
                val token = tokenRepository.getToken()
                val request = AddSensorRequest(homeId, name, type)
                val response = sensorApi.addSensor(token, request)

                if (response.isSuccessful) {
                    loadSensors()
                    _errorMessage.value = null
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseErrorMessage(errorBody)
                    _errorMessage.value = errorMessage
                    Timber.tag("SensorViewModel").e(errorMessage ?: "Unknown error")
                }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                _errorMessage.value = errorMessage
                Timber.tag("SensorViewModel").e(errorMessage)
            }
        }
    }

    suspend fun deleteSensor(sensorId: String) {
        try {
            val token = tokenRepository.getToken()
            val response = sensorApi.deleteSensor(token, sensorId)
            if (response.isSuccessful) {
                loadSensors()
                _errorMessage.value = null
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody)
                _errorMessage.value = errorMessage
                Timber.tag("SensorViewModel").e(errorMessage ?: "Unknown error")
            }
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            _errorMessage.value = errorMessage
            Timber.tag("SensorViewModel").e(errorMessage)
        }
    }

    suspend fun archiveSensor(sensorId: String) {
        try {
            val token = tokenRepository.getToken()
            val response = sensorApi.archiveSensor(token, sensorId)
            if (response.isSuccessful) {
                loadSensors()
                _errorMessage.value = null
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody)
                _errorMessage.value = errorMessage
                Timber.tag("SensorViewModel").e(errorMessage ?: "Unknown error")
            }
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            _errorMessage.value = errorMessage
            Timber.tag("SensorViewModel").e(errorMessage)
        }
    }

    suspend fun unArchiveSensor(sensorId: String) {
        try {
            val token = tokenRepository.getToken()
            val response = sensorApi.unArchiveSensor(token, sensorId)
            if (response.isSuccessful) {
                loadSensors()
                _errorMessage.value = null
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody)
                _errorMessage.value = errorMessage
                Timber.tag("SensorViewModel").e(errorMessage ?: "Unknown error")
            }
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            _errorMessage.value = errorMessage
            Timber.tag("SensorViewModel").e(errorMessage)
        }
    }

    suspend fun setActiveSensor(sensorId: String, isActive: Boolean): Boolean {
        try {
            val token = tokenRepository.getToken()
            val request = ActiveSensorRequest(isActive)
            val response = sensorApi.setActiveSensor(token, sensorId, request)

            if (response.isSuccessful) {
                val updatedSensors = _sensorsState.value.map {
                    if (it.sensor_id == sensorId) it.copy(is_active = isActive) else it
                }
                _sensorsState.value = updatedSensors
                _errorMessage.value = null
                return true
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody)
                _errorMessage.value = errorMessage
                Timber.tag("SensorViewModel").e(errorMessage ?: "Unknown error")
                return false
            }
        } catch (e: Exception) {
            val errorMessage = "Network error: ${e.message}"
            _errorMessage.value = errorMessage
            Timber.tag("SensorViewModel").e(errorMessage)
            return false
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