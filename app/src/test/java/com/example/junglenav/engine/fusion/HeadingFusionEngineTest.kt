package com.example.junglenav.engine.fusion

import com.example.junglenav.engine.sensors.InertialSample
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadingFusionEngineTest {
    @Test
    fun gyroDominatesShortTermHeadingWhileMagnetometerCorrectsSlowly() {
        val engine = HeadingFusionEngine()

        engine.update(
            inertialSample = InertialSample.gyroTurn(
                yawRateDegPerSec = 0f,
                timestampEpochMs = 1_000L,
                magneticHeadingDegrees = 70f,
            ),
            magneticHeadingDegrees = 70f,
        )

        val estimate = engine.update(
            inertialSample = InertialSample.gyroTurn(
                yawRateDegPerSec = 10f,
                timestampEpochMs = 2_000L,
                magneticHeadingDegrees = 110f,
            ),
            magneticHeadingDegrees = 110f,
        )

        assertTrue(estimate.sourceLabel.contains("gyro"))
        assertTrue(estimate.headingDegrees > 70f)
        assertTrue(estimate.headingDegrees < 90f)
    }
}
