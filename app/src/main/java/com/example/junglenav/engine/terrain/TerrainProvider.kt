package com.example.junglenav.engine.terrain

interface TerrainProvider {
    fun sample(latitude: Double, longitude: Double): TerrainSample?
}
