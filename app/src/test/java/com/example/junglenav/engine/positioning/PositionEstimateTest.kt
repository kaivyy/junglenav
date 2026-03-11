package com.example.junglenav.engine.positioning

import com.example.junglenav.core.model.PositionMode
import org.junit.Assert.assertEquals
import org.junit.Test

class PositionEstimateTest {
    @Test
    fun fromSampleCarriesNavigationSourceAndGnssMetadata() {
        val sample = LocationSample(
            latitude = -6.2,
            longitude = 106.8,
            accuracyMeters = 8f,
            speedMps = 1.2f,
            bearingDegrees = 45f,
            timestampEpochMs = 10_000L,
            gnssSatellitesUsed = 14,
            gnssConstellations = setOf("GPS", "GLONASS", "Galileo"),
        )

        val estimate = PositionEstimate.fromSample(sample)

        assertEquals(PositionMode.GNSS_LOCKED, estimate.mode)
        assertEquals(NavigationSource.GNSS, estimate.source)
        assertEquals(14, estimate.gnssSatellitesUsed)
        assertEquals(setOf("GPS", "GLONASS", "Galileo"), estimate.gnssConstellations)
    }
}
