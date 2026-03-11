package com.example.junglenav.system.offline

import android.content.Context
import android.util.DisplayMetrics
import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.core.model.OfflineRegionCatalogItem
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition

class MapLibreOfflineRegionService(
    context: Context,
) : OfflineRegionDownloadService {
    private val appContext = context.applicationContext

    override suspend fun downloadRegion(
        catalogItem: OfflineRegionCatalogItem,
        onProgress: (OfflineRegionDownloadProgress) -> Unit,
    ): MapPackage = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            val definition = OfflineTilePyramidRegionDefinition(
                catalogItem.styleUri,
                LatLngBounds.from(
                    catalogItem.northLatitude,
                    catalogItem.eastLongitude,
                    catalogItem.southLatitude,
                    catalogItem.westLongitude,
                ),
                catalogItem.minZoom,
                catalogItem.maxZoom,
                appContext.resources.displayMetrics.offlinePixelRatio(),
            )

            OfflineManager.getInstance(appContext).createOfflineRegion(
                definition,
                catalogItem.id.encodeToByteArray(),
                object : OfflineManager.CreateOfflineRegionCallback {
                    override fun onCreate(region: OfflineRegion) {
                        region.setObserver(
                            object : OfflineRegion.OfflineRegionObserver {
                                override fun onStatusChanged(status: OfflineRegionStatus) {
                                    onProgress(
                                        OfflineRegionDownloadProgress(
                                            completedResources = status.completedResourceCount,
                                            requiredResources = status.requiredResourceCount,
                                            completedBytes = status.completedResourceSize,
                                        ),
                                    )

                                    if (status.isComplete && continuation.isActive) {
                                        region.setDownloadState(OfflineRegion.STATE_INACTIVE)
                                        continuation.resume(
                                            MapPackage(
                                                id = catalogItem.id,
                                                name = catalogItem.name,
                                                version = "1.0",
                                                sizeBytes = status.completedResourceSize
                                                    .takeIf { it > 0L }
                                                    ?: catalogItem.estimatedSizeBytes,
                                                isActive = false,
                                                filePath = catalogItem.styleUri,
                                                checksum = "offline-region-${region.id}",
                                                isDownloaded = true,
                                                offlineRegionId = region.id,
                                                centerLatitude = catalogItem.centerLatitude,
                                                centerLongitude = catalogItem.centerLongitude,
                                            ),
                                        )
                                    }
                                }

                                override fun onError(error: OfflineRegionError) {
                                    if (continuation.isActive) {
                                        continuation.resumeWithException(
                                            IllegalStateException(
                                                error.message ?: error.reason,
                                            ),
                                        )
                                    }
                                }

                                override fun mapboxTileCountLimitExceeded(limit: Long) {
                                    if (continuation.isActive) {
                                        continuation.resumeWithException(
                                            IllegalStateException("Offline tile count limit exceeded: $limit"),
                                        )
                                    }
                                }
                            },
                        )
                        region.setDownloadState(OfflineRegion.STATE_ACTIVE)
                    }

                    override fun onError(error: String) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(IllegalStateException(error))
                        }
                    }
                },
            )
        }
    }

    override suspend fun deleteRegion(mapPackage: MapPackage) {
        val regionId = mapPackage.offlineRegionId ?: return

        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine<Unit> { continuation ->
                OfflineManager.getInstance(appContext).getOfflineRegion(
                    regionId,
                    object : OfflineManager.GetOfflineRegionCallback {
                        override fun onRegion(region: OfflineRegion) {
                            region.delete(
                                object : OfflineRegion.OfflineRegionDeleteCallback {
                                    override fun onDelete() {
                                        if (continuation.isActive) {
                                            continuation.resume(Unit)
                                        }
                                    }

                                    override fun onError(error: String) {
                                        if (continuation.isActive) {
                                            continuation.resumeWithException(IllegalStateException(error))
                                        }
                                    }
                                },
                            )
                        }

                        override fun onRegionNotFound() {
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }

                        override fun onError(error: String) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(IllegalStateException(error))
                            }
                        }
                    },
                )
            }
        }
    }
}

private fun DisplayMetrics.offlinePixelRatio(): Float {
    return density.takeIf { it > 0f } ?: 1f
}
