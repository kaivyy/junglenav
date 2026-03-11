package com.example.junglenav.feature.waypoint

import com.example.junglenav.core.model.Waypoint
import com.example.junglenav.data.repository.WaypointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WaypointListViewModelTest {
    @Test
    fun addWaypointPushesItemIntoUiState() = runTest {
        val repo = FakeWaypointRepository()
        val viewModel = WaypointListViewModel(repo)

        viewModel.saveWaypoint(name = "Water Source", latitude = -6.0, longitude = 106.0)

        assertEquals("Water Source", viewModel.uiState.value.items.single().name)
    }
}

private class FakeWaypointRepository : WaypointRepository {
    private val state = MutableStateFlow<List<Waypoint>>(emptyList())

    override fun observeWaypoints(): Flow<List<Waypoint>> = state

    override suspend fun save(waypoint: Waypoint) {
        state.value = listOf(waypoint) + state.value
    }

    override suspend fun delete(id: String) = Unit
}
