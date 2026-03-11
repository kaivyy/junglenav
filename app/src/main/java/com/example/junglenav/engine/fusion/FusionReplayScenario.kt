package com.example.junglenav.engine.fusion

import com.example.junglenav.engine.positioning.LocationSample

data class FusionReplayScenario(
    val samples: List<LocationSample>,
) {
    companion object {
        fun basicForestWalk(): FusionReplayScenario {
            return FusionReplayScenario(
                samples = listOf(
                    LocationSample(
                        latitude = -6.2000,
                        longitude = 106.8000,
                        accuracyMeters = 18f,
                        speedMps = 1.1f,
                        bearingDegrees = 92f,
                        timestampEpochMs = 1_000L,
                        gnssSatellitesUsed = 6,
                        gnssConstellations = setOf("GPS", "GLONASS"),
                    ),
                    LocationSample(
                        latitude = -6.2002,
                        longitude = 106.8003,
                        accuracyMeters = 16f,
                        speedMps = 1.0f,
                        bearingDegrees = 93f,
                        timestampEpochMs = 2_000L,
                        gnssSatellitesUsed = 7,
                        gnssConstellations = setOf("GPS", "GLONASS", "Galileo"),
                    ),
                ),
            )
        }
    }
}
