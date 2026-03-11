package com.example.junglenav.engine.fusion

import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.engine.positioning.LocationSample
import com.example.junglenav.engine.positioning.PositionEstimate

class PositionStateMachine(
    private val staleThresholdMs: Long = 15_000L,
    private val lowConfidenceThresholdMs: Long = 60_000L,
) {
    var currentState: PositionEstimate = PositionEstimate.noFix()
        private set

    fun onLocationSample(sample: LocationSample) {
        currentState = PositionEstimate.fromSample(sample)
    }

    fun onTick(nowEpochMs: Long) {
        val age = nowEpochMs - currentState.timestampEpochMs
        currentState = when {
            currentState.mode == PositionMode.NO_FIX -> currentState
            age >= lowConfidenceThresholdMs -> currentState.copy(
                mode = PositionMode.DR_LOW_CONF,
                confidence = 20,
            )

            age >= staleThresholdMs -> currentState.copy(
                mode = PositionMode.DR_ACTIVE,
                confidence = 45,
            )

            else -> currentState.copy(mode = PositionMode.GNSS_LOCKED)
        }
    }
}
