package com.example.junglenav.data.repository

import com.example.junglenav.core.model.Waypoint
import com.example.junglenav.data.db.dao.WaypointDao
import com.example.junglenav.data.db.entity.WaypointEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineWaypointRepository(
    private val waypointDao: WaypointDao,
) : WaypointRepository {
    override fun observeWaypoints(): Flow<List<Waypoint>> {
        return waypointDao.observeAll().map { entities ->
            entities.map(WaypointEntity::toModel)
        }
    }

    override suspend fun save(waypoint: Waypoint) {
        waypointDao.upsert(waypoint.toEntity())
    }

    override suspend fun delete(id: String) {
        waypointDao.delete(id)
    }
}

private fun WaypointEntity.toModel(): Waypoint {
    return Waypoint(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        altitudeMeters = altitudeMeters,
        category = category,
        note = note,
        createdAtEpochMs = createdAtEpochMs,
    )
}

private fun Waypoint.toEntity(): WaypointEntity {
    return WaypointEntity(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        altitudeMeters = altitudeMeters,
        category = category,
        note = note,
        createdAtEpochMs = createdAtEpochMs,
    )
}
