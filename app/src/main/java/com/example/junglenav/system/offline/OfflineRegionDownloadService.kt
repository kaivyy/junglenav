package com.example.junglenav.system.offline

import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.core.model.OfflineRegionCatalogItem

interface OfflineRegionDownloadService {
    suspend fun downloadRegion(
        catalogItem: OfflineRegionCatalogItem,
        onProgress: (OfflineRegionDownloadProgress) -> Unit,
    ): MapPackage

    suspend fun deleteRegion(mapPackage: MapPackage)
}
