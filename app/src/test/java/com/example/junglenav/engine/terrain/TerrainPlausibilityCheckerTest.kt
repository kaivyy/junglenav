package com.example.junglenav.engine.terrain

import org.junit.Assert.assertFalse
import org.junit.Test

class TerrainPlausibilityCheckerTest {
    @Test
    fun estimateInRiverChannelIsMarkedImplausibleWhenNearbyTrailExists() {
        val checker = TerrainPlausibilityChecker()

        val plausible = checker.isPlausible(
            elevation = 120f,
            slopeDegrees = 3f,
            onRiver = true,
            onTrail = false,
        )

        assertFalse(plausible)
    }
}
