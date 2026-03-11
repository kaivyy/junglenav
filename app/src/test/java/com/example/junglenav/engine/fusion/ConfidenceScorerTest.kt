package com.example.junglenav.engine.fusion

import org.junit.Assert.assertTrue
import org.junit.Test

class ConfidenceScorerTest {
    @Test
    fun deadReckoningConfidenceDecaysWithTimeAndHeadingUncertainty() {
        val scorer = ConfidenceScorer()

        val score = scorer.scoreDeadReckoning(
            elapsedMs = 20_000L,
            headingUncertaintyDegrees = 18f,
        )

        assertTrue(score < 60)
    }
}
