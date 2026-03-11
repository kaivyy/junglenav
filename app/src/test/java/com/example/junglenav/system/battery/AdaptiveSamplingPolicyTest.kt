package com.example.junglenav.system.battery

import com.example.junglenav.core.model.OperationMode
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveSamplingPolicyTest {
    @Test
    fun runningInPatrolModeUsesFasterCadenceThanBasePatrolProfile() {
        val profile = AdaptiveSamplingPolicy.forContext(
            operationMode = OperationMode.PATROL,
            motionState = MotionState.RUNNING,
        )

        assertTrue(profile.locationIntervalMs < BatteryPolicy.forMode(OperationMode.PATROL).locationIntervalMs)
        assertTrue(profile.sensorIntervalMs < BatteryPolicy.forMode(OperationMode.PATROL).sensorIntervalMs)
    }
}
