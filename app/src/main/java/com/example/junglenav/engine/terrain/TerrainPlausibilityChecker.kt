package com.example.junglenav.engine.terrain

class TerrainPlausibilityChecker {
    fun isPlausible(
        elevation: Float,
        slopeDegrees: Float,
        onRiver: Boolean,
        onTrail: Boolean,
    ): Boolean {
        if (onRiver && !onTrail) return false
        if (slopeDegrees > 55f) return false
        return elevation > -500f
    }

    fun penaltyFor(sample: TerrainSample): Int {
        return when {
            !isPlausible(
                elevation = sample.elevationMeters,
                slopeDegrees = sample.slopeDegrees,
                onRiver = sample.onRiver,
                onTrail = sample.onTrail,
            ) -> 20
            sample.vegetationDensity > 0.8f -> 8
            else -> 0
        }
    }
}
