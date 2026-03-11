package com.example.junglenav.engine.terrain

class SrtmTerrainProvider : TerrainProvider {
    override fun sample(latitude: Double, longitude: Double): TerrainSample? {
        val elevation = ((latitude + longitude) * 10).toFloat()
        val slope = ((kotlin.math.abs(latitude) + kotlin.math.abs(longitude)) % 12).toFloat()
        return TerrainSample(
            elevationMeters = elevation,
            slopeDegrees = slope,
            onRiver = false,
            onTrail = true,
            vegetationDensity = 0.4f,
        )
    }
}
