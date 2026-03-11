package com.example.junglenav.system.replay

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ReplayLocationSourceTest {
    @Test
    fun replaySourceEmitsSamplesInRecordedOrder() = runBlocking {
        val source = ReplayLocationSource(
            samples = listOf(
                RecordedLocationSample(timestampEpochMs = 100L, latitude = 1.0, longitude = 2.0),
                RecordedLocationSample(timestampEpochMs = 200L, latitude = 3.0, longitude = 4.0),
            ),
        )

        val firstSample = source.observeLocationSamples().first()

        assertEquals(100L, firstSample.timestampEpochMs)
        assertEquals(1.0, firstSample.latitude, 0.0)
    }
}
