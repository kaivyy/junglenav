package com.example.junglenav.engine.fusion

import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.engine.positioning.LocationSample
import com.example.junglenav.engine.positioning.NavigationSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PositionFusionEngineTest {
    @Test
    fun degradedGnssFallsBackToFusedThenDeadReckoningInsteadOfFreezing() {
        val engine = PositionFusionEngine()

        engine.onLocationSample(
            LocationSample(
                latitude = -6.2,
                longitude = 106.8,
                accuracyMeters = 32f,
                speedMps = 1.0f,
                bearingDegrees = 0f,
                timestampEpochMs = 1_000L,
                gnssSatellitesUsed = 5,
                gnssConstellations = setOf("GPS"),
            ),
        )

        assertEquals(PositionMode.FUSED, engine.estimate.value.mode)
        assertEquals(NavigationSource.FUSED, engine.estimate.value.source)

        engine.onTick(nowEpochMs = 18_000L)

        assertTrue(engine.estimate.value.mode in setOf(PositionMode.FUSED, PositionMode.DR_ACTIVE))
    }
}
