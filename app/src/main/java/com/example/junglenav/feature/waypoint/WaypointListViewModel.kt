package com.example.junglenav.feature.waypoint

import com.example.junglenav.core.model.Waypoint
import com.example.junglenav.data.repository.WaypointRepository
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

data class WaypointListUiState(
    val items: List<Waypoint> = emptyList(),
)

class WaypointListViewModel(
    private val repository: WaypointRepository,
) {
    private val _uiState = MutableStateFlow(WaypointListUiState())
    val uiState: StateFlow<WaypointListUiState> = _uiState.asStateFlow()

    fun saveWaypoint(
        name: String,
        latitude: Double,
        longitude: Double,
        altitudeMeters: Double? = null,
        category: String? = null,
        note: String? = null,
    ) {
        val waypoint = Waypoint(
            id = UUID.randomUUID().toString(),
            name = name,
            latitude = latitude,
            longitude = longitude,
            altitudeMeters = altitudeMeters,
            category = category,
            note = note,
            createdAtEpochMs = System.currentTimeMillis(),
        )

        runBlocking {
            repository.save(waypoint)
        }

        _uiState.update { state -> state.copy(items = listOf(waypoint) + state.items) }
    }

    fun deleteWaypoint(id: String) {
        runBlocking {
            repository.delete(id)
        }

        _uiState.update { state ->
            state.copy(items = state.items.filterNot { it.id == id })
        }
    }
}
