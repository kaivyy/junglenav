package com.example.junglenav.engine.deadreckoning

import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.engine.fusion.ConfidenceScorer
import com.example.junglenav.engine.positioning.NavigationSource
import com.example.junglenav.engine.positioning.PositionEstimate

class DeadReckoningEngine(
    private val stepStrideModel: StepStrideModel = StepStrideModel(),
    private val confidenceScorer: ConfidenceScorer = ConfidenceScorer(),
) {
    fun projectWithoutGnss(
        elapsedMs: Long,
        stepCountDelta: Int,
        headingDegrees: Float,
        anchorEstimate: PositionEstimate = PositionEstimate.noFix(),
    ): PositionEstimate {
        val projectedDistance = stepStrideModel.estimateDistanceMeters(stepCountDelta)
        val confidence = confidenceScorer.scoreDeadReckoning(
            elapsedMs = elapsedMs,
            headingUncertaintyDegrees = projectedDistance.coerceAtLeast(5f),
        )

        return anchorEstimate.copy(
            mode = if (confidence < 35) PositionMode.DR_LOW_CONF else PositionMode.DR_ACTIVE,
            source = NavigationSource.DEAD_RECKONING,
            confidence = confidence,
            bearingDegrees = headingDegrees,
            driftSeconds = elapsedMs / 1_000f,
            headingAccuracyDegrees = projectedDistance.coerceAtMost(45f),
            timestampEpochMs = anchorEstimate.timestampEpochMs + elapsedMs,
        )
    }
}
