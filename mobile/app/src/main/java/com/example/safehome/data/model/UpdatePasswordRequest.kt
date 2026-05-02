package com.example.safehome.data.model

data class UpdatePasswordRequest(
    val old_password: String,
    val new_password: String,
)