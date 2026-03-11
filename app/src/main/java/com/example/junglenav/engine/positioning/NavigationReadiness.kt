package com.example.junglenav.engine.positioning

enum class NavigationFeedMode {
    LIVE,
    REPLAY,
    FALLBACK,
    PENDING,
    UNAVAILABLE,
}

data class NavigationReadiness(
    val gnssMode: NavigationFeedMode = NavigationFeedMode.PENDING,
    val inertialMode: NavigationFeedMode = NavigationFeedMode.PENDING,
    val barometerMode: NavigationFeedMode = NavigationFeedMode.PENDING,
) {
    fun sensorAvailabilityLines(): List<String> {
        return listOf(
            buildLine(label = "GNSS adapter", mode = gnssMode),
            buildLine(label = "IMU adapter", mode = inertialMode),
            buildLine(label = "Barometer adapter", mode = barometerMode),
        )
    }

    private fun buildLine(
        label: String,
        mode: NavigationFeedMode,
    ): String {
        return when (mode) {
            NavigationFeedMode.LIVE -> "$label ready (live)"
            NavigationFeedMode.REPLAY -> "$label ready (replay)"
            NavigationFeedMode.FALLBACK -> "$label fallback active"
            NavigationFeedMode.PENDING -> "$label pending"
            NavigationFeedMode.UNAVAILABLE -> "$label unavailable"
        }
    }
}
