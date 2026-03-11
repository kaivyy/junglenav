package com.example.junglenav.core.model

data class OfflineRegionCatalogItem(
    val id: String,
    val name: String,
    val summary: String,
    val styleUri: String,
    val estimatedSizeBytes: Long,
    val northLatitude: Double,
    val eastLongitude: Double,
    val southLatitude: Double,
    val westLongitude: Double,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val minZoom: Double,
    val maxZoom: Double,
)
