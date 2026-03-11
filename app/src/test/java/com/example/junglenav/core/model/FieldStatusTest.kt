package com.example.junglenav.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldStatusTest {
    @Test
    fun emergencyModeStartsInFusedStateWithHighRefresh() {
        val status = FieldStatus.forMode(OperationMode.EMERGENCY)

        assertEquals(PositionMode.FUSED, status.mode)
        assertEquals(1_000L, status.locationIntervalMs)
        assertEquals(ConfidenceLevel.MEDIUM, status.confidenceLevel)
    }
}
