package com.example.junglenav.core.model

data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val speedMps: Double?,
    val headingDegrees: Double?,
    val recordedAtEpochMs: Long,
    val positionMode: PositionMode,
    val confidence: Int,
)
