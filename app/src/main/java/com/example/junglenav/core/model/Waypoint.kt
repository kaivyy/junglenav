package com.example.junglenav.core.model

data class Waypoint(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val category: String?,
    val note: String?,
    val createdAtEpochMs: Long,
)
