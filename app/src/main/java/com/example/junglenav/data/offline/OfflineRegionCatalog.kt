package com.example.junglenav.data.offline

import com.example.junglenav.core.model.OfflineRegionCatalogItem
object OfflineRegionCatalog {
    fun items(): List<OfflineRegionCatalogItem> {
        return listOf(
            OfflineRegionCatalogItem(
                id = "jakarta-core",
                name = "Jakarta Core",
                summary = "Compact urban field pack around central Jakarta.",
                styleUri = "https://demotiles.maplibre.org/style.json",
                estimatedSizeBytes = 18_000_000L,
                northLatitude = -6.15,
                eastLongitude = 106.90,
                southLatitude = -6.30,
                westLongitude = 106.75,
                centerLatitude = -6.20,
                centerLongitude = 106.82,
                minZoom = 10.0,
                maxZoom = 12.0,
            ),
            OfflineRegionCatalogItem(
                id = "bogor-trails",
                name = "Bogor Trails",
                summary = "Rainy foothill pack for denser vegetation and contour checks.",
                styleUri = "https://demotiles.maplibre.org/style.json",
                estimatedSizeBytes = 20_000_000L,
                northLatitude = -6.43,
                eastLongitude = 106.92,
                southLatitude = -6.69,
                westLongitude = 106.65,
                centerLatitude = -6.57,
                centerLongitude = 106.80,
                minZoom = 10.0,
                maxZoom = 12.0,
            ),
            OfflineRegionCatalogItem(
                id = "bandung-highlands",
                name = "Bandung Highlands",
                summary = "Highland route pack for hill movement and slope checks.",
                styleUri = "https://demotiles.maplibre.org/style.json",
                estimatedSizeBytes = 22_000_000L,
                northLatitude = -6.78,
                eastLongitude = 107.74,
                southLatitude = -6.99,
                westLongitude = 107.50,
                centerLatitude = -6.91,
                centerLongitude = 107.61,
                minZoom = 10.0,
                maxZoom = 12.0,
            ),
        )
    }
}
