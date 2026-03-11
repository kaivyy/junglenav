package com.example.junglenav.system.replay

import com.example.junglenav.engine.positioning.LocationSample
import com.example.junglenav.engine.positioning.LocationSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class RecordedLocationSample(
    val timestampEpochMs: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 10f,
    val speedMps: Float = 0f,
    val bearingDegrees: Float = 0f,
    val gnssSatellitesUsed: Int? = null,
    val gnssConstellations: Set<String> = emptySet(),
)

class ReplayLocationSource(
    private val samples: List<RecordedLocationSample>,
) : LocationSource {
    override fun observeLocationSamples(): Flow<LocationSample> = flow {
        samples
            .sortedBy { it.timestampEpochMs }
            .forEach { sample ->
                emit(
                    LocationSample(
                        latitude = sample.latitude,
                        longitude = sample.longitude,
                        accuracyMeters = sample.accuracyMeters,
                        speedMps = sample.speedMps,
                        bearingDegrees = sample.bearingDegrees,
                        timestampEpochMs = sample.timestampEpochMs,
                        gnssSatellitesUsed = sample.gnssSatellitesUsed,
                        gnssConstellations = sample.gnssConstellations,
                    ),
                )
            }
    }
}
