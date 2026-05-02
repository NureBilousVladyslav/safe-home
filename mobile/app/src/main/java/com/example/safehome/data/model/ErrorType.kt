package com.example.safehome.data.model

sealed class ErrorType {
    abstract val message: String

    data class InternalError(override val message: String) : ErrorType()
    data class NetworkError(override val message: String) : ErrorType()
    data class ServerError(override val message: String, val code: Int) : ErrorType()
}