package com.example.junglenav.core.model

data class RemoteMapPackCatalogItem(
    val id: String,
    val name: String,
    val summary: String,
    val downloadUrl: String,
    val estimatedSizeBytes: Long,
    val northLatitude: Double,
    val eastLongitude: Double,
    val southLatitude: Double,
    val westLongitude: Double,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val minZoom: Double,
    val maxZoom: Double,
    val publisher: String? = null,
    val trustHint: MapPackTrust = MapPackTrust.UNVERIFIED,
    val layers: MapPackLayerSet = MapPackLayerSet(),
)
