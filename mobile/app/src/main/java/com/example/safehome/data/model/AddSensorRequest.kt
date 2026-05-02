package com.example.safehome.data.model

data class AddSensorRequest(
    val home_id: String,
    val name: String,
    val type: String,
)