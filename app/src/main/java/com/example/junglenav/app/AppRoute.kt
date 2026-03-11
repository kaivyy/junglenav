package com.example.junglenav.app

sealed class AppRoute(val route: String, val label: String) {
    data object Map : AppRoute(route = "map", label = "Map")
    data object Waypoints : AppRoute(route = "waypoints", label = "Waypoints")
    data object Tracks : AppRoute(route = "tracks", label = "Tracks")
    data object Packages : AppRoute(route = "packages", label = "Packages")
    data object Diagnostics : AppRoute(route = "diagnostics", label = "Diagnostics")
    data object Settings : AppRoute(route = "settings", label = "Settings")

    companion object {
        val startDestination: String
            get() = Map.route

        val topLevelRoutes: List<AppRoute>
            get() = listOf(
                Map,
                Waypoints,
                Tracks,
                Packages,
                Diagnostics,
                Settings,
            )
    }
}
