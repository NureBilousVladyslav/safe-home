package com.example.safehome.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.safehome.data.local.entity.HomeEntity

@Dao
interface HomeDao {
    @Query("SELECT * FROM homes")
    suspend fun getAllHomes(): List<HomeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomes(homes: List<HomeEntity>)

    @Query("DELETE FROM homes")
    suspend fun clearHomes()
}