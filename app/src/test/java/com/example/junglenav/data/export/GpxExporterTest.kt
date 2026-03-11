package com.example.junglenav.data.export

import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.core.model.TrackPoint
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxExporterTest {
    @Test
    fun embedsPositionModeInsideTrackPointExtensions() {
        val xml = GpxExporter().export(
            name = "Patrol",
            points = listOf(
                TrackPoint(
                    latitude = -6.2,
                    longitude = 106.8,
                    altitudeMeters = 130.0,
                    speedMps = 1.3,
                    headingDegrees = 87.0,
                    recordedAtEpochMs = 1L,
                    positionMode = PositionMode.DR_ACTIVE,
                    confidence = 42,
                )
            )
        )

        assertTrue(xml.contains("<junglenav:position_mode>DR_ACTIVE</junglenav:position_mode>"))
    }
}
