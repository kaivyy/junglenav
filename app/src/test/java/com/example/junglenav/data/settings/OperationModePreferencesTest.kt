package com.example.junglenav.data.settings

import com.example.junglenav.core.model.OperationMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OperationModePreferencesTest {
    @Test
    fun defaultsToPatrolMode() = runTest {
        val prefs = OperationModePreferences(storage = mutableMapOf())

        assertEquals(OperationMode.PATROL, prefs.readMode())
    }
}
