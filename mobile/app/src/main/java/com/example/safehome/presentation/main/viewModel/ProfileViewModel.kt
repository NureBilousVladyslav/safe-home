package com.example.safehome.presentation.main.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safehome.data.api.UserApi
import com.example.safehome.data.model.ErrorResponse
import com.example.safehome.data.model.GetUserResponse
import com.example.safehome.data.repo.TokenRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private var tokenRepository: TokenRepository,
    private val userApi: UserApi,
) : ViewModel() {
    private val _userState = MutableStateFlow<GetUserResponse?>(null)
    val userState: StateFlow<GetUserResponse?> = _userState.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            try {
                val token = tokenRepository.getToken()
                val response = userApi.getUser(token)

                if (response.isSuccessful) {
                    _userState.value = response.body()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: JsonSyntaxException) {
                        "Unknown error: $e"
                    }
                    _userState.value = null
                    Timber.tag("SensorViewModel").e(errorMessage)
                }
            } catch (e: Exception) {
                Timber.tag("SensorViewModel").e("Network error: ${e.message}")
            }
        }
    }

    fun logout(){
        tokenRepository.clearToken()
    }
}