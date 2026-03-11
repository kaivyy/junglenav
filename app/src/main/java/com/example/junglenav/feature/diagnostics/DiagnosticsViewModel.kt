package com.example.junglenav.feature.diagnostics

import com.example.junglenav.core.model.OperationMode
import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.engine.positioning.NavigationFeedMode
import com.example.junglenav.engine.positioning.NavigationReadiness
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DiagnosticsUiState(
    val operationMode: OperationMode = OperationMode.PATROL,
    val positionMode: PositionMode = PositionMode.NO_FIX,
    val navigationSourceLabel: String = "FUSED",
    val lastReliableFixAgeMs: Long? = null,
    val activePackageId: String? = null,
    val sensorAvailability: List<String> = NavigationReadiness().sensorAvailabilityLines(),
    val eventLog: List<String> = listOf("Diagnostics ready"),
)

class DiagnosticsViewModel {
    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    fun syncState(
        operationMode: OperationMode,
        positionMode: PositionMode,
        navigationSourceLabel: String,
        lastReliableFixAgeMs: Long?,
        activePackageId: String?,
    ) {
        _uiState.update {
            it.copy(
                operationMode = operationMode,
                positionMode = positionMode,
                navigationSourceLabel = navigationSourceLabel,
                lastReliableFixAgeMs = lastReliableFixAgeMs,
                activePackageId = activePackageId,
            )
        }
    }

    fun syncSensorReadiness(
        gnssReady: Boolean,
        inertialReady: Boolean,
        barometerReady: Boolean,
    ) {
        syncNavigationReadiness(
            NavigationReadiness(
                gnssMode = if (gnssReady) NavigationFeedMode.LIVE else NavigationFeedMode.UNAVAILABLE,
                inertialMode = if (inertialReady) NavigationFeedMode.LIVE else NavigationFeedMode.UNAVAILABLE,
                barometerMode = if (barometerReady) NavigationFeedMode.LIVE else NavigationFeedMode.UNAVAILABLE,
            ),
        )
    }

    fun syncNavigationReadiness(readiness: NavigationReadiness) {
        _uiState.update {
            it.copy(sensorAvailability = readiness.sensorAvailabilityLines())
        }
    }

    fun recordEvent(message: String) {
        _uiState.update { state ->
            state.copy(eventLog = (listOf(message) + state.eventLog).distinct().take(10))
        }
    }
}
