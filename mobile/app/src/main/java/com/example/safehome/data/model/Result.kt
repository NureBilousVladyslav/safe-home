package com.example.safehome.data.model

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val errorType: ErrorType) : Result<Nothing>()
    object Loading : Result<Nothing>()
}