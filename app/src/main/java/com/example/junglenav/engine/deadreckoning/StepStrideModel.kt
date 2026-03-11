package com.example.junglenav.engine.deadreckoning

class StepStrideModel(
    private val strideLengthMeters: Float = 0.75f,
) {
    fun estimateDistanceMeters(stepCountDelta: Int): Float {
        return stepCountDelta.coerceAtLeast(0) * strideLengthMeters
    }
}
