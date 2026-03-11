package com.example.junglenav.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class MapViewportState : MapSurface {
    var loadedStylePath: String? by mutableStateOf(null)
        private set
    var centerLatitude: Double by mutableStateOf(-6.2)
        private set
    var centerLongitude: Double by mutableStateOf(106.8)
        private set
    var isTrackVisible: Boolean by mutableStateOf(false)
        private set
    var areWaypointsVisible: Boolean by mutableStateOf(true)
        private set

    override fun loadStyle(packagePath: String) {
        loadedStylePath = packagePath
    }

    override fun setCenter(latitude: Double, longitude: Double) {
        centerLatitude = latitude
        centerLongitude = longitude
    }

    override fun showTrack(enabled: Boolean) {
        isTrackVisible = enabled
    }

    override fun showWaypoints(enabled: Boolean) {
        areWaypointsVisible = enabled
    }
}

@Composable
fun rememberMapViewportState(): MapViewportState = remember { MapViewportState() }
