package com.example.safehome.data.model

data class SensorDto(
    val sensor_id: String,
    val name: String,
    val type: String,
    val created_at: String,
    val short_code: String,
    val is_archived: Boolean,
    val is_active: Boolean,
    val is_closed: Boolean,
    val is_security_breached: Boolean,
)