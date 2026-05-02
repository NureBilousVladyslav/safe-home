package com.example.safehome.presentation.model

data class Home(
    val homeId: String,
    val name: String,
    val address: String?,
    val isArchived: Boolean,
)