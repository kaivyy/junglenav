package com.example.junglenav.feature.track

import com.example.junglenav.core.model.TrackPoint
import com.example.junglenav.core.model.TrackSession
import com.example.junglenav.data.repository.TrackRepository
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

data class TrackUiState(
    val isRecording: Boolean = false,
    val activeSessionId: String? = null,
    val lastSavedPointCount: Int = 0,
    val sessions: List<TrackSession> = emptyList(),
)

class TrackViewModel(
    private val repository: TrackRepository,
) {
    private val _uiState = MutableStateFlow(TrackUiState())
    val uiState: StateFlow<TrackUiState> = _uiState.asStateFlow()

    fun startRecording() {
        val now = System.currentTimeMillis()
        val session = TrackSession(
            id = UUID.randomUUID().toString(),
            name = "Track ${now}",
            startedAtEpochMs = now,
            endedAtEpochMs = null,
            totalDistanceMeters = 0.0,
            totalDurationMs = 0L,
            status = "recording",
        )

        runBlocking {
            repository.saveSession(session)
        }

        _uiState.update { state ->
            state.copy(
                isRecording = true,
                activeSessionId = session.id,
                lastSavedPointCount = 0,
                sessions = listOf(session) + state.sessions,
            )
        }
    }

    fun onTrackPoint(point: TrackPoint) {
        val sessionId = _uiState.value.activeSessionId ?: return

        runBlocking {
            repository.appendPoint(sessionId, point)
        }

        _uiState.update { state ->
            state.copy(lastSavedPointCount = state.lastSavedPointCount + 1)
        }
    }

    fun stopRecording() {
        val state = _uiState.value
        val sessionId = state.activeSessionId ?: return
        val currentSession = state.sessions.firstOrNull { it.id == sessionId } ?: return
        val now = System.currentTimeMillis()
        val finished = currentSession.copy(
            endedAtEpochMs = now,
            totalDurationMs = now - currentSession.startedAtEpochMs,
            status = "finished",
        )

        runBlocking {
            repository.saveSession(finished)
        }

        _uiState.update { previous ->
            previous.copy(
                isRecording = false,
                activeSessionId = null,
                sessions = previous.sessions.map { session ->
                    if (session.id == sessionId) finished else session
                },
            )
        }
    }
}
