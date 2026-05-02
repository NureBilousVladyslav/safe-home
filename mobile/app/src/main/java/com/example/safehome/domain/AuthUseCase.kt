package com.example.safehome.domain

import com.example.safehome.data.model.Result
import com.example.safehome.data.repo.AuthRepository
import javax.inject.Inject

class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend fun isUserAuthorized(email: String, password: String): Result<Boolean> {
        return authRepository.login(email, password)
    }

    suspend fun isTokenExpired(): Result<Boolean> {
        return authRepository.verifyToken()
    }

    suspend fun firebaseGoogleAuth(firebaseToken: String): Result<Boolean> {
        return authRepository.googleLogin(firebaseToken)
    }
}