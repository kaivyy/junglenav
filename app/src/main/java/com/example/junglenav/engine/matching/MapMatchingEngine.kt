package com.example.junglenav.engine.matching

class MapMatchingEngine {
    fun match(
        latitude: Double,
        longitude: Double,
        candidatePaths: List<PathCandidate>,
    ): MatchedPosition {
        val nearest = candidatePaths.minByOrNull { candidate ->
            val latDelta = candidate.latitude - latitude
            val lonDelta = candidate.longitude - longitude
            (latDelta * latDelta) + (lonDelta * lonDelta)
        } ?: return MatchedPosition(
            pathId = "raw-position",
            latitude = latitude,
            longitude = longitude,
            penaltyScore = 0,
        )

        return MatchedPosition(
            pathId = nearest.pathId,
            latitude = nearest.latitude,
            longitude = nearest.longitude,
            penaltyScore = 4,
        )
    }
}
