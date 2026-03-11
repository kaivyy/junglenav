package com.example.junglenav.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waypoints")
data class WaypointEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val category: String? = null,
    val note: String? = null,
    val createdAtEpochMs: Long,
)
