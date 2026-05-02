package com.example.safehome.data.model

data class HomeDto(
    val home_id: String,
    val name: String,
    val address: String,
    val created_at: String,
    val is_archived: Boolean,
    val default_mode_id: String,
    val default_mode_name: String,
)