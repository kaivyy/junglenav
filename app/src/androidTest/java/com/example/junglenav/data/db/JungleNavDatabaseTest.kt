package com.example.junglenav.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.junglenav.data.db.entity.WaypointEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JungleNavDatabaseTest {
    @Test
    fun waypointRoundTripPersistsFields() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = JungleNavDatabase.inMemory(context)
        val waypoint = WaypointEntity(
            id = "wp-1",
            name = "Camp",
            latitude = -6.2,
            longitude = 106.8,
            altitudeMeters = 120.0,
            note = "Base camp",
            createdAtEpochMs = 1L,
        )

        db.waypointDao().upsert(waypoint)

        assertEquals("Camp", db.waypointDao().observeAllOnce().single().name)
    }
}
