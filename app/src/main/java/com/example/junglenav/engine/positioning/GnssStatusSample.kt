package com.example.junglenav.engine.positioning

data class GnssStatusSample(
    val satellitesUsed: Int,
    val constellationNames: Set<String>,
    val timestampEpochMs: Long,
)
