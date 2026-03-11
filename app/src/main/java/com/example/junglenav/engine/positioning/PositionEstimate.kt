package com.example.junglenav.engine.positioning

import com.example.junglenav.core.model.PositionMode

data class PositionEstimate(
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Float?,
    val speedMps: Float?,
    val bearingDegrees: Float?,
    val headingAccuracyDegrees: Float?,
    val timestampEpochMs: Long,
    val mode: PositionMode,
    val source: NavigationSource,
    val confidence: Int,
    val driftSeconds: Float?,
    val gnssSatellitesUsed: Int?,
    val gnssConstellations: Set<String>,
) {
    companion object {
        fun noFix(): PositionEstimate {
            return PositionEstimate(
                latitude = null,
                longitude = null,
                accuracyMeters = null,
                speedMps = null,
                bearingDegrees = null,
                headingAccuracyDegrees = null,
                timestampEpochMs = 0L,
                mode = PositionMode.NO_FIX,
                source = NavigationSource.FUSED,
                confidence = 0,
                driftSeconds = null,
                gnssSatellitesUsed = null,
                gnssConstellations = emptySet(),
            )
        }

        fun fromSample(sample: LocationSample): PositionEstimate {
            return PositionEstimate(
                latitude = sample.latitude,
                longitude = sample.longitude,
                accuracyMeters = sample.accuracyMeters,
                speedMps = sample.speedMps,
                bearingDegrees = sample.bearingDegrees,
                headingAccuracyDegrees = sample.accuracyMeters,
                timestampEpochMs = sample.timestampEpochMs,
                mode = PositionMode.GNSS_LOCKED,
                source = NavigationSource.GNSS,
                confidence = 80,
                driftSeconds = 0f,
                gnssSatellitesUsed = sample.gnssSatellitesUsed,
                gnssConstellations = sample.gnssConstellations,
            )
        }
    }
}
