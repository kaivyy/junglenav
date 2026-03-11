package com.example.junglenav.engine.terrain

data class TerrainSample(
    val elevationMeters: Float,
    val slopeDegrees: Float,
    val onRiver: Boolean,
    val onTrail: Boolean,
    val vegetationDensity: Float,
)
