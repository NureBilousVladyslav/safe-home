package com.example.safehome.domain

import com.example.safehome.data.model.Result
import com.example.safehome.data.repo.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun sendResetPassword(email: String): Result<Boolean> {
        return when (val result = authRepository.resetPassword(email)) {
            is Result.Success -> {
                Result.Success(true)
            }
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }
}