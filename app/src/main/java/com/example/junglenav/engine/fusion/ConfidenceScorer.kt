package com.example.junglenav.engine.fusion

import kotlin.math.roundToInt

class ConfidenceScorer {
    fun scoreDeadReckoning(
        elapsedMs: Long,
        headingUncertaintyDegrees: Float,
    ): Int {
        val timePenalty = (elapsedMs / 1_000f) * 1.2f
        val headingPenalty = headingUncertaintyDegrees * 0.9f
        return (80f - timePenalty - headingPenalty)
            .roundToInt()
            .coerceIn(15, 80)
    }
}
