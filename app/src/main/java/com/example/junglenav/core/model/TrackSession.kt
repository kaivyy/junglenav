package com.example.junglenav.core.model

data class TrackSession(
    val id: String,
    val name: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long?,
    val totalDistanceMeters: Double,
    val totalDurationMs: Long,
    val status: String,
)
