package com.example.junglenav.system.battery

import com.example.junglenav.core.model.OperationMode

enum class MotionState {
    STILL,
    WALKING,
    RUNNING,
}

object AdaptiveSamplingPolicy {
    fun forContext(
        operationMode: OperationMode,
        motionState: MotionState,
    ): BatteryProfile {
        val base = BatteryPolicy.forMode(operationMode)
        return when (motionState) {
            MotionState.STILL -> base.copy(
                locationIntervalMs = (base.locationIntervalMs * 2).coerceAtMost(20_000L),
                sensorIntervalMs = (base.sensorIntervalMs * 2).coerceAtMost(12_000L),
            )

            MotionState.WALKING -> base

            MotionState.RUNNING -> base.copy(
                locationIntervalMs = (base.locationIntervalMs * 0.6f).toLong().coerceAtLeast(500L),
                sensorIntervalMs = (base.sensorIntervalMs * 0.6f).toLong().coerceAtLeast(250L),
            )
        }
    }
}
