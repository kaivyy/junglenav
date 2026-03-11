package com.example.junglenav.data.repository

import com.example.junglenav.core.model.Waypoint
import kotlinx.coroutines.flow.Flow

interface WaypointRepository {
    fun observeWaypoints(): Flow<List<Waypoint>>
    suspend fun save(waypoint: Waypoint)
    suspend fun delete(id: String)
}
