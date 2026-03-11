package com.example.junglenav.core.model

data class MapPackLayerSet(
    val topoVector: Boolean = true,
    val hillshadeRaster: Boolean = false,
    val imageryRaster: Boolean = false,
)
