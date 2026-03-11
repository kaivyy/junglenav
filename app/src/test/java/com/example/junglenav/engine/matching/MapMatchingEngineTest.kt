package com.example.junglenav.engine.matching

import org.junit.Assert.assertEquals
import org.junit.Test

class MapMatchingEngineTest {
    @Test
    fun offTrailEstimateIsPulledTowardNearestValidPath() {
        val engine = MapMatchingEngine()

        val matched = engine.match(
            latitude = -6.2001,
            longitude = 106.8012,
            candidatePaths = listOf(
                PathCandidate(
                    pathId = "ridge-trail",
                    latitude = -6.2000,
                    longitude = 106.8010,
                ),
            ),
        )

        assertEquals("ridge-trail", matched.pathId)
    }
}
