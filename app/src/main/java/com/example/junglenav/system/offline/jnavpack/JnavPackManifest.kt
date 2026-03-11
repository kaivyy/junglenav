package com.example.junglenav.system.offline.jnavpack

import com.example.junglenav.core.model.MapPackLayerSet

data class JnavPackManifest(
    val id: String,
    val name: String,
    val version: String,
    val packageFormat: Int,
    val bounds: JnavPackBounds,
    val center: JnavPackCenter,
    val minZoom: Double,
    val maxZoom: Double,
    val layers: MapPackLayerSet,
    val stylePath: String,
    val vectorTilesPath: String?,
    val hillshadeTilesPath: String?,
    val imageryTilesPath: String?,
    val publisher: String?,
    val signature: String?,
    val checksumSha256: String?,
)

data class JnavPackBounds(
    val westLongitude: Double,
    val southLatitude: Double,
    val eastLongitude: Double,
    val northLatitude: Double,
)

data class JnavPackCenter(
    val longitude: Double,
    val latitude: Double,
)
