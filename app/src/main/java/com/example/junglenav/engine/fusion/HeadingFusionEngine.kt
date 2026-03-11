package com.example.junglenav.engine.fusion

import com.example.junglenav.engine.sensors.InertialSample
import kotlin.math.PI

class HeadingFusionEngine(
    private val calibrationGate: MagneticCalibrationGate = MagneticCalibrationGate(),
) {
    private var lastHeadingDegrees: Float? = null
    private var lastTimestampEpochMs: Long? = null

    fun update(
        inertialSample: InertialSample,
        magneticHeadingDegrees: Float? = inertialSample.magneticHeadingDegrees,
    ): HeadingEstimate {
        val previousHeading = lastHeadingDegrees
        val previousTimestamp = lastTimestampEpochMs

        val predictedHeading = if (previousHeading == null || previousTimestamp == null) {
            normalizeDegrees(magneticHeadingDegrees ?: 0f)
        } else {
            val elapsedSeconds = (inertialSample.timestampEpochMs - previousTimestamp) / 1_000f
            val deltaDegrees = ((inertialSample.gyroZRadPerSec * elapsedSeconds) * (180f / PI.toFloat()))
            normalizeDegrees(previousHeading + deltaDegrees)
        }

        val correctedHeading = if (calibrationGate.shouldApplyCorrection(predictedHeading, magneticHeadingDegrees)) {
            val magneticHeading = magneticHeadingDegrees ?: predictedHeading
            normalizeDegrees((predictedHeading * 0.92f) + (magneticHeading * 0.08f))
        } else {
            predictedHeading
        }

        lastHeadingDegrees = correctedHeading
        lastTimestampEpochMs = inertialSample.timestampEpochMs

        return HeadingEstimate(
            headingDegrees = correctedHeading,
            confidence = if (magneticHeadingDegrees != null) 78 else 62,
            sourceLabel = if (magneticHeadingDegrees != null) {
                "gyro-stabilized + mag-corrected"
            } else {
                "gyro-stabilized"
            },
        )
    }

    private fun normalizeDegrees(value: Float): Float {
        val normalized = value % 360f
        return if (normalized < 0f) normalized + 360f else normalized
    }
}
