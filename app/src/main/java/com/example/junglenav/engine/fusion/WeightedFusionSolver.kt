package com.example.junglenav.engine.fusion

import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.engine.positioning.NavigationSource
import com.example.junglenav.engine.positioning.PositionEstimate

class WeightedFusionSolver : FusionSolver {
    override fun solve(snapshot: HybridNavigationSnapshot): PositionEstimate {
        val ageMs = snapshot.locationAgeMs
        val locationSample = snapshot.latestLocationSample

        val baseEstimate = when {
            locationSample != null && ageMs != null && ageMs <= 15_000L -> {
                val satellites = locationSample.gnssSatellitesUsed ?: 0
                val isHealthyGnss = locationSample.accuracyMeters <= 15f && satellites >= 6
                PositionEstimate.fromSample(locationSample).copy(
                    mode = if (isHealthyGnss) PositionMode.GNSS_LOCKED else PositionMode.FUSED,
                    source = if (isHealthyGnss) NavigationSource.GNSS else NavigationSource.FUSED,
                    confidence = if (isHealthyGnss) 86 else 68,
                    headingAccuracyDegrees = snapshot.latestHeadingEstimate?.confidence?.toFloat()
                        ?: locationSample.accuracyMeters,
                    bearingDegrees = snapshot.latestHeadingEstimate?.headingDegrees ?: locationSample.bearingDegrees,
                )
            }

            snapshot.deadReckoningEstimate != null -> snapshot.deadReckoningEstimate

            else -> PositionEstimate.noFix()
        }

        val matchedEstimate = snapshot.matchedPosition?.let { matched ->
            if (baseEstimate.mode == PositionMode.NO_FIX) {
                baseEstimate
            } else {
                baseEstimate.copy(
                    latitude = matched.latitude,
                    longitude = matched.longitude,
                    source = NavigationSource.MAP_MATCHED,
                    confidence = (baseEstimate.confidence - matched.penaltyScore).coerceAtLeast(20),
                )
            }
        } ?: baseEstimate

        return if (snapshot.terrainPenalty > 0 && matchedEstimate.mode != PositionMode.NO_FIX) {
            matchedEstimate.copy(
                source = NavigationSource.TERRAIN_CORRECTED,
                confidence = (matchedEstimate.confidence - snapshot.terrainPenalty).coerceAtLeast(15),
            )
        } else {
            matchedEstimate
        }
    }
}
