package com.example.junglenav.system.battery

import com.example.junglenav.core.model.OperationMode
import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryPolicyTest {
    @Test
    fun batterySaverUsesSlowestLocationCadence() {
        assertEquals(10_000L, BatteryPolicy.forMode(OperationMode.BATTERY_SAVER).locationIntervalMs)
    }
}
