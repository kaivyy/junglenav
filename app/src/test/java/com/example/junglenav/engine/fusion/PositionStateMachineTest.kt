package com.example.junglenav.engine.fusion

import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.engine.positioning.LocationSample
import org.junit.Assert.assertEquals
import org.junit.Test

class PositionStateMachineTest {
    @Test
    fun staleGnssDropsIntoDeadReckoningState() {
        val machine = PositionStateMachine()
        machine.onLocationSample(
            LocationSample(
                latitude = -6.2,
                longitude = 106.8,
                accuracyMeters = 8f,
                speedMps = 1.2f,
                bearingDegrees = 90f,
                timestampEpochMs = 1_000L,
            )
        )

        machine.onTick(nowEpochMs = 20_000L)

        assertEquals(PositionMode.DR_ACTIVE, machine.currentState.mode)
    }
}
