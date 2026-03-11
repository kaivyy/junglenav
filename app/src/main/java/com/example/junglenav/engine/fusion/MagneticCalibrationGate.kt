package com.example.junglenav.engine.fusion

import kotlin.math.abs

class MagneticCalibrationGate(
    private val maxCorrectionDifferenceDegrees: Float = 60f,
) {
    fun shouldApplyCorrection(
        predictedHeadingDegrees: Float,
        magneticHeadingDegrees: Float?,
    ): Boolean {
        val magneticHeading = magneticHeadingDegrees ?: return false
        val difference = angularDifferenceDegrees(predictedHeadingDegrees, magneticHeading)
        return difference <= maxCorrectionDifferenceDegrees
    }

    private fun angularDifferenceDegrees(
        first: Float,
        second: Float,
    ): Float {
        val difference = abs(first - second) % 360f
        return if (difference > 180f) 360f - difference else difference
    }
}
