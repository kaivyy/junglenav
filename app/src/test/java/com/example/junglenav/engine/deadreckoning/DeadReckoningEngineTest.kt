package com.example.junglenav.engine.deadreckoning

import com.example.junglenav.engine.positioning.NavigationSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeadReckoningEngineTest {
    @Test
    fun confidenceDropsAsDeadReckoningRunsWithoutFreshGnss() {
        val engine = DeadReckoningEngine()

        val estimate = engine.projectWithoutGnss(
            elapsedMs = 20_000L,
            stepCountDelta = 24,
            headingDegrees = 90f,
        )

        assertTrue(estimate.confidence < 60)
        assertEquals(NavigationSource.DEAD_RECKONING, estimate.source)
    }
}
