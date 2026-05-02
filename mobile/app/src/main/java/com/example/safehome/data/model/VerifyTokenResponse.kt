package com.example.safehome.data.model

data class VerifyTokenResponse(
    val userId: String,
    val valid: Boolean,
)