package com.example.safehome.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "homes")
data class HomeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val createdAt: String,
    val defaultModeId: String,
    val defaultModeName: String,
    val isArchived: Boolean
)