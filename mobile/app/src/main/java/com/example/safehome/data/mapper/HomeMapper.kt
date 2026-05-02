package com.example.safehome.data.mapper

import com.example.safehome.data.local.entity.HomeEntity
import com.example.safehome.presentation.model.Home
import com.example.safehome.data.model.HomeDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeMapper @Inject constructor() {
    fun toUi(dto: HomeDto): Home {
        return Home(
            homeId = dto.home_id,
            name = dto.name,
            address = dto.address,
            isArchived = dto.is_archived
        )
    }

    fun toUi(entity: HomeEntity): Home {
        return Home(
            homeId = entity.id,
            name = entity.name,
            address = entity.address,
            isArchived = entity.isArchived
        )
    }

    fun dtoToEntity(dto: HomeDto): HomeEntity {
        return HomeEntity(
            id = dto.home_id,
            name = dto.name,
            address = dto.address,
            createdAt = dto.created_at,
            defaultModeId = dto.default_mode_id,
            defaultModeName = dto.default_mode_name,
            isArchived = dto.is_archived
        )
    }

    fun entityToDto(entity: HomeEntity): HomeDto {
        return HomeDto(
            home_id = entity.id,
            name = entity.name,
            address = entity.address,
            created_at = entity.createdAt,
            default_mode_id = entity.defaultModeId,
            default_mode_name = entity.defaultModeName,
            is_archived = entity.isArchived
        )
    }
}