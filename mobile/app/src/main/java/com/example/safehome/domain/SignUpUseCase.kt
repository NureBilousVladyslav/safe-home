package com.example.safehome.domain

import com.example.safehome.data.model.ErrorType
import com.example.safehome.data.model.Result
import com.example.safehome.data.repo.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun isRegister(name: String, email: String, password: String): Result<Boolean> {
        return when (val result = authRepository.register(name, email, password)) {
            is Result.Success -> {
                Result.Success(true)
            }
            is Result.Error -> {
                when (val error = result.errorType) {
                    is ErrorType.ServerError -> error.message
                    is ErrorType.NetworkError -> error.message
                    is ErrorType.InternalError -> error.message
                }
                result
            }
            is Result.Loading -> Result.Loading
        }
    }
}