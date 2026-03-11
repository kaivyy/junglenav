package com.example.junglenav.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_packages")
data class MapPackageEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val sizeBytes: Long,
    val filePath: String,
    val checksum: String,
    val isActive: Boolean,
    val isDownloaded: Boolean,
    val offlineRegionId: Long?,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val installRootPath: String?,
    val manifestPath: String?,
    val source: String,
    val trust: String,
    val publisher: String?,
    val requiresActivationConfirmation: Boolean,
    val hasTopoVector: Boolean,
    val hasHillshadeRaster: Boolean,
    val hasImageryRaster: Boolean,
    val thumbnailPath: String?,
)
