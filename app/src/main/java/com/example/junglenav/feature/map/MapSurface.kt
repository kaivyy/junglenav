package com.example.junglenav.feature.map

interface MapSurface {
    fun loadStyle(packagePath: String)

    fun setCenter(latitude: Double, longitude: Double)

    fun showTrack(enabled: Boolean)

    fun showWaypoints(enabled: Boolean)
}
