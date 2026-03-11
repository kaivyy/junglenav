package com.example.junglenav.feature.track

import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.core.model.TrackPoint
import com.example.junglenav.core.model.TrackSession
import com.example.junglenav.data.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackViewModelTest {
    @Test
    fun startStopCycleProducesFinishedTrackState() = runTest {
        val viewModel = TrackViewModel(FakeTrackRepository())

        viewModel.startRecording()
        viewModel.onTrackPoint(
            TrackPoint(
                latitude = -6.2,
                longitude = 106.8,
                altitudeMeters = 120.0,
                speedMps = 1.0,
                headingDegrees = 90.0,
                recordedAtEpochMs = 1L,
                positionMode = PositionMode.GNSS_LOCKED,
                confidence = 92,
            )
        )
        viewModel.stopRecording()

        assertEquals(false, viewModel.uiState.value.isRecording)
        assertEquals(1, viewModel.uiState.value.lastSavedPointCount)
    }
}

private class FakeTrackRepository : TrackRepository {
    private val sessions = MutableStateFlow<List<TrackSession>>(emptyList())

    override fun observeSessions(): Flow<List<TrackSession>> = sessions

    override suspend fun saveSession(session: TrackSession) {
        sessions.value = listOf(session) + sessions.value
    }

    override suspend fun appendPoint(sessionId: String, point: TrackPoint) = Unit
}
