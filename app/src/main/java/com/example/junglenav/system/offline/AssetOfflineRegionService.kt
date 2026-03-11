package com.example.junglenav.system.offline

import android.content.Context
import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.core.model.OfflineRegionCatalogItem
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetOfflineRegionService(
    context: Context,
) : OfflineRegionDownloadService {
    private val appContext = context.applicationContext

    override suspend fun downloadRegion(
        catalogItem: OfflineRegionCatalogItem,
        onProgress: (OfflineRegionDownloadProgress) -> Unit,
    ): MapPackage = withContext(Dispatchers.IO) {
        val sourceFolder = "offline_regions/${catalogItem.id}"
        val targetFolder = File(appContext.filesDir, "offline-region-cache/${catalogItem.id}")
        if (targetFolder.exists()) {
            targetFolder.deleteRecursively()
        }
        targetFolder.mkdirs()

        val assetManager = appContext.assets
        val assetFiles = assetManager.list(sourceFolder)?.sorted().orEmpty()
        val totalResources = assetFiles.size + 1L

        assetFiles.forEachIndexed { index, assetName ->
            assetManager.open("$sourceFolder/$assetName").use { input ->
                File(targetFolder, assetName).outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            onProgress(
                OfflineRegionDownloadProgress(
                    completedResources = index + 1L,
                    requiredResources = totalResources,
                    completedBytes = targetFolder.directorySizeBytes(),
                ),
            )
        }

        val styleFile = File(targetFolder, "style.json")
        styleFile.writeText(
            buildOfflineStyleJson(
                trailsUri = File(targetFolder, "trails.geojson").toUriString(),
                contoursUri = File(targetFolder, "contours.geojson").toUriString(),
                waterUri = File(targetFolder, "water.geojson").toUriString(),
                landmarksUri = File(targetFolder, "landmarks.geojson").toUriString(),
            ),
        )

        onProgress(
            OfflineRegionDownloadProgress(
                completedResources = totalResources,
                requiredResources = totalResources,
                completedBytes = targetFolder.directorySizeBytes(),
            ),
        )

        MapPackage(
            id = catalogItem.id,
            name = catalogItem.name,
            version = "asset-1.0",
            sizeBytes = targetFolder.directorySizeBytes(),
            isActive = false,
            filePath = styleFile.toUriString(),
            checksum = "asset-pack-${catalogItem.id}",
            isDownloaded = true,
            centerLatitude = catalogItem.centerLatitude,
            centerLongitude = catalogItem.centerLongitude,
        )
    }

    override suspend fun deleteRegion(mapPackage: MapPackage) = withContext(Dispatchers.IO) {
        val styleFile = mapPackage.filePath.toFile()
        val parentFolder = styleFile.parentFile ?: return@withContext
        if (parentFolder.exists()) {
            parentFolder.deleteRecursively()
        }
    }
}

private fun File.directorySizeBytes(): Long {
    return walkTopDown()
        .filter(File::isFile)
        .fold(0L) { total, file -> total + file.length() }
}

private fun File.toUriString(): String = toURI().toString()

private fun String.toFile(): File {
    return if (startsWith("file:")) {
        File(java.net.URI(this))
    } else {
        File(this)
    }
}

private fun buildOfflineStyleJson(
    trailsUri: String,
    contoursUri: String,
    waterUri: String,
    landmarksUri: String,
): String {
    return """
        {
          "version": 8,
          "name": "JungleNav Offline Pack",
          "sources": {
            "trails": { "type": "geojson", "data": "$trailsUri" },
            "contours": { "type": "geojson", "data": "$contoursUri" },
            "water": { "type": "geojson", "data": "$waterUri" },
            "landmarks": { "type": "geojson", "data": "$landmarksUri" }
          },
          "layers": [
            {
              "id": "background",
              "type": "background",
              "paint": { "background-color": "#eef2e4" }
            },
            {
              "id": "water-fill",
              "type": "fill",
              "source": "water",
              "paint": {
                "fill-color": "#98d1f2",
                "fill-opacity": 0.82
              }
            },
            {
              "id": "contours-line",
              "type": "line",
              "source": "contours",
              "paint": {
                "line-color": "#c0cda8",
                "line-width": 1.2,
                "line-opacity": 0.85
              }
            },
            {
              "id": "trails-line",
              "type": "line",
              "source": "trails",
              "paint": {
                "line-color": "#446638",
                "line-width": 3.0,
                "line-opacity": 0.96
              }
            },
            {
              "id": "landmarks-circle",
              "type": "circle",
              "source": "landmarks",
              "paint": {
                "circle-radius": 5.0,
                "circle-color": "#d96a1c",
                "circle-stroke-width": 1.5,
                "circle-stroke-color": "#fff7ec"
              }
            }
          ]
        }
    """.trimIndent()
}
