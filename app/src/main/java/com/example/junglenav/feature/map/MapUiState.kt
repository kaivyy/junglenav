package com.example.junglenav.feature.map

import com.example.junglenav.core.model.ConfidenceLevel
import com.example.junglenav.core.model.MapPackLayerSet
import com.example.junglenav.core.model.OperationMode
import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.engine.positioning.NavigationSource

data class MapUiState(
    val positionMode: PositionMode = PositionMode.NO_FIX,
    val confidenceLevel: ConfidenceLevel = ConfidenceLevel.LOW,
    val confidenceLabel: String = "No fix",
    val activeTargetName: String = "No target",
    val distanceLabel: String = "--",
    val isRecording: Boolean = false,
    val operationMode: OperationMode = OperationMode.PATROL,
    val activePackageName: String? = null,
    val activePackagePath: String? = null,
    val activePackTrustLabel: String? = null,
    val styleUri: String = MapViewModel.DEFAULT_STYLE_URI,
    val isUsingFallbackStyle: Boolean = true,
    val mapSourceLabel: String = "Live fallback style",
    val isMapReady: Boolean = false,
    val mapLoadError: String? = null,
    val availableLayers: MapPackLayerSet = MapPackLayerSet(),
    val isTopoEnabled: Boolean = true,
    val isHillshadeEnabled: Boolean = false,
    val isImageryEnabled: Boolean = false,
    val navigationSourceLabel: String = NavigationSource.FUSED.name,
    val navigationConfidenceScore: Int = 0,
    val hasGpsFix: Boolean = false,
    val gpsLatitude: Double? = null,
    val gpsLongitude: Double? = null,
    val gpsStatusLabel: String = "GPS standby",
    val isFollowingGps: Boolean = false,
    val isOfflineRegionExpanded: Boolean = false,
) {
    val canShowHillshadeToggle: Boolean
        get() = availableLayers.hillshadeRaster

    val canShowImageryToggle: Boolean
        get() = availableLayers.imageryRaster
}
