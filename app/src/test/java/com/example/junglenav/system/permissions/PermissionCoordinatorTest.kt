package com.example.junglenav.system.permissions

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionCoordinatorTest {
    @Test
    fun requiresBackgroundLocationOnlyWhenTrackRecordingEnabled() {
        val coordinator = PermissionCoordinator()
        assertEquals(false, coordinator.needsBackgroundLocation(isRecording = false))
        assertEquals(true, coordinator.needsBackgroundLocation(isRecording = true))
    }
}
