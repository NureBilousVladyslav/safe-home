package com.example.safehome.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.safehome.data.local.dao.HomeDao
import com.example.safehome.data.local.entity.HomeEntity

@Database(entities = [HomeEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun homeDao(): HomeDao
}