package com.example.junglenav.engine.positioning

data class LocationSample(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val speedMps: Float,
    val bearingDegrees: Float,
    val timestampEpochMs: Long,
    val gnssSatellitesUsed: Int? = null,
    val gnssConstellations: Set<String> = emptySet(),
)
