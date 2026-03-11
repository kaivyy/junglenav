package com.example.junglenav.feature.map

import com.example.junglenav.core.model.ConfidenceLevel
import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.core.model.OperationMode
import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.engine.positioning.NavigationSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MapViewModel {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun onPositionStateChanged(
        mode: PositionMode,
        confidenceLevel: ConfidenceLevel,
    ) {
        _uiState.update { state ->
            state.copy(
                positionMode = mode,
                confidenceLevel = confidenceLevel,
                confidenceLabel = confidenceLabel(mode = mode, confidenceLevel = confidenceLevel),
            )
        }
    }

    fun onRecordingChanged(isRecording: Boolean) {
        _uiState.update { it.copy(isRecording = isRecording) }
    }

    fun onTargetChanged(
        activeTargetName: String,
        distanceLabel: String,
    ) {
        _uiState.update {
            it.copy(
                activeTargetName = activeTargetName,
                distanceLabel = distanceLabel,
            )
        }
    }

    fun onOperationModeChanged(operationMode: OperationMode) {
        _uiState.update { it.copy(operationMode = operationMode) }
    }

    fun onActivePackageChanged(
        activePackageName: String?,
        activePackagePath: String?,
        isOfflinePackageReady: Boolean,
    ) {
        val resolvedStyleUri = resolveStyleUri(activePackagePath)
        val isFallback = !isOfflinePackageReady && resolvedStyleUri == DEFAULT_STYLE_URI
        _uiState.update {
            it.copy(
                activePackageName = activePackageName,
                activePackagePath = activePackagePath,
                styleUri = resolvedStyleUri,
                isUsingFallbackStyle = isFallback,
                mapSourceLabel = when {
                    isOfflinePackageReady -> "Offline region cache"
                    isFallback -> "Live fallback style"
                    else -> "Remote package style"
                },
                isMapReady = false,
                mapLoadError = null,
            )
        }
    }

    fun onOfflineBundleResolved(
        mapPackage: MapPackage,
        resolvedStyleUri: String,
    ) {
        _uiState.update {
            it.copy(
                activePackageName = mapPackage.name,
                activePackagePath = mapPackage.filePath,
                activePackTrustLabel = mapPackage.trust.readableLabel(),
                styleUri = resolvedStyleUri,
                isUsingFallbackStyle = false,
                mapSourceLabel = "Offline jnavpack",
                isMapReady = false,
                mapLoadError = null,
                availableLayers = mapPackage.layers,
                isTopoEnabled = mapPackage.layers.topoVector,
                isHillshadeEnabled = mapPackage.layers.hillshadeRaster,
                isImageryEnabled = false,
            )
        }
    }

    fun onMapStyleLoaded() {
        _uiState.update {
            it.copy(
                isMapReady = true,
                mapLoadError = null,
            )
        }
    }

    fun onMapStyleLoadFailed(message: String) {
        _uiState.update { state ->
            if (!state.isUsingFallbackStyle) {
                state.copy(
                    styleUri = DEFAULT_STYLE_URI,
                    isUsingFallbackStyle = true,
                    mapSourceLabel = "Live fallback style",
                    isMapReady = false,
                    mapLoadError = message,
                )
            } else {
                state.copy(
                    isMapReady = false,
                    mapLoadError = message,
                )
            }
        }
    }

    fun onLiveGpsFix(
        latitude: Double,
        longitude: Double,
        source: NavigationSource,
        confidence: Int,
    ) {
        _uiState.update {
            it.copy(
                hasGpsFix = true,
                gpsLatitude = latitude,
                gpsLongitude = longitude,
                gpsStatusLabel = "Live GPS ready",
                navigationSourceLabel = source.name,
                navigationConfidenceScore = confidence,
            )
        }
    }

    fun onGpsUnavailable(message: String = "GPS unavailable") {
        _uiState.update {
            it.copy(
                hasGpsFix = false,
                gpsLatitude = null,
                gpsLongitude = null,
                gpsStatusLabel = message,
                isFollowingGps = false,
            )
        }
    }

    fun onNavigationSourceChanged(
        source: NavigationSource,
        confidence: Int,
    ) {
        _uiState.update {
            it.copy(
                navigationSourceLabel = source.name,
                navigationConfidenceScore = confidence,
            )
        }
    }

    fun setOfflineRegionExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isOfflineRegionExpanded = expanded) }
    }

    fun setGpsFollowing(following: Boolean) {
        _uiState.update { it.copy(isFollowingGps = following) }
    }

    fun setTopoEnabled(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                isTopoEnabled = if (state.availableLayers.topoVector) enabled else state.isTopoEnabled,
            )
        }
    }

    fun setHillshadeEnabled(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                isHillshadeEnabled = if (state.availableLayers.hillshadeRaster) enabled else false,
            )
        }
    }

    fun setImageryEnabled(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                isImageryEnabled = if (state.availableLayers.imageryRaster) enabled else false,
            )
        }
    }

    private fun confidenceLabel(
        mode: PositionMode,
        confidenceLevel: ConfidenceLevel,
    ): String {
        return when {
            mode == PositionMode.NO_FIX -> "No fix"
            confidenceLevel == ConfidenceLevel.HIGH -> "High confidence"
            confidenceLevel == ConfidenceLevel.MEDIUM -> "Medium confidence"
            else -> "Low confidence"
        }
    }

    private fun resolveStyleUri(activePackagePath: String?): String {
        if (activePackagePath.isNullOrBlank()) {
            return DEFAULT_STYLE_URI
        }

        return when {
            activePackagePath.startsWith("https://", ignoreCase = true) -> activePackagePath
            activePackagePath.startsWith("http://", ignoreCase = true) -> activePackagePath
            activePackagePath.startsWith("file://", ignoreCase = true) -> activePackagePath
            activePackagePath.startsWith("asset://", ignoreCase = true) -> activePackagePath
            activePackagePath.startsWith("content://", ignoreCase = true) -> activePackagePath
            activePackagePath.endsWith(".json", ignoreCase = true) -> activePackagePath
            else -> DEFAULT_STYLE_URI
        }
    }

    companion object {
        const val DEFAULT_STYLE_URI = "https://tiles.openfreemap.org/styles/liberty"
    }
}

private fun com.example.junglenav.core.model.MapPackTrust.readableLabel(): String {
    return when (this) {
        com.example.junglenav.core.model.MapPackTrust.VERIFIED -> "Verified"
        com.example.junglenav.core.model.MapPackTrust.UNVERIFIED -> "Unverified"
    }
}
