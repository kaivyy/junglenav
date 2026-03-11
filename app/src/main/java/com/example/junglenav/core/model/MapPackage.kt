package com.example.junglenav.core.model

data class MapPackage(
    val id: String,
    val name: String,
    val version: String,
    val sizeBytes: Long,
    val isActive: Boolean,
    val filePath: String,
    val checksum: String,
    val isDownloaded: Boolean = false,
    val offlineRegionId: Long? = null,
    val centerLatitude: Double = -6.2,
    val centerLongitude: Double = 106.8,
    val installRootPath: String? = null,
    val manifestPath: String? = null,
    val source: MapPackSource = MapPackSource.BUNDLED_ASSET,
    val trust: MapPackTrust = MapPackTrust.VERIFIED,
    val publisher: String? = null,
    val requiresActivationConfirmation: Boolean = false,
    val layers: MapPackLayerSet = MapPackLayerSet(),
    val thumbnailPath: String? = null,
)
