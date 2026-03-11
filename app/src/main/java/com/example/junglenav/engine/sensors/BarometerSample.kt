package com.example.junglenav.engine.sensors

data class BarometerSample(
    val pressureHpa: Float,
    val altitudeMeters: Float?,
    val timestampEpochMs: Long,
)
