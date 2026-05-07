package com.example.safehome.presentation.main.viewModel

import androidx.lifecycle.ViewModel
import com.example.safehome.data.api.UserApi
import com.example.safehome.data.model.ErrorResponse
import com.example.safehome.data.model.MessageResponse
import com.example.safehome.data.model.UpdatePasswordRequest
import com.example.safehome.data.repo.TokenRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val userApi: UserApi
) : ViewModel() {

    suspend fun updatePassword(currentPassword: String, newPassword: String): MessageResponse? {
        return try {
            val token = tokenRepository.getToken()
            if (token == null) {
                Timber.tag("ChangePasswordViewModel").e("No token available")
                return MessageResponse("No token available")
            }

            val request = UpdatePasswordRequest(currentPassword, newPassword)
            val response = userApi.updatePassword(token, request)

            if (response.isSuccessful) {
                response.body()
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).error
                } catch (e: JsonSyntaxException) {
                    Timber.tag("ChangePasswordViewModel").e("Failed to parse error body: ${e.message}")
                    "Unknown error: ${e.message}"
                }
                Timber.tag("ChangePasswordViewModel").e("Error ${response.code()}: $errorMessage")
                MessageResponse(errorMessage)
            }
        } catch (e: Exception) {
            Timber.tag("ChangePasswordViewModel").e("Network error: ${e.message}")
            MessageResponse("Network error: ${e.message}")
        }
    }
}