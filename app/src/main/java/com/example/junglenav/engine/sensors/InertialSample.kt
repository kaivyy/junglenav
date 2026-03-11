package com.example.junglenav.engine.sensors

data class InertialSample(
    val accelerationXMps2: Float,
    val accelerationYMps2: Float,
    val accelerationZMps2: Float,
    val gyroXRadPerSec: Float,
    val gyroYRadPerSec: Float,
    val gyroZRadPerSec: Float,
    val magneticHeadingDegrees: Float?,
    val timestampEpochMs: Long,
) {
    companion object {
        fun gyroTurn(
            yawRateDegPerSec: Float,
            timestampEpochMs: Long,
            magneticHeadingDegrees: Float? = null,
        ): InertialSample {
            return InertialSample(
                accelerationXMps2 = 0f,
                accelerationYMps2 = 0f,
                accelerationZMps2 = 9.81f,
                gyroXRadPerSec = 0f,
                gyroYRadPerSec = 0f,
                gyroZRadPerSec = Math.toRadians(yawRateDegPerSec.toDouble()).toFloat(),
                magneticHeadingDegrees = magneticHeadingDegrees,
                timestampEpochMs = timestampEpochMs,
            )
        }
    }
}
