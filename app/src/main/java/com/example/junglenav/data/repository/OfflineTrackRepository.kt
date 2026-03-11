package com.example.junglenav.data.repository

import com.example.junglenav.core.model.TrackPoint
import com.example.junglenav.core.model.TrackSession
import com.example.junglenav.data.db.dao.TrackDao
import com.example.junglenav.data.db.entity.TrackPointEntity
import com.example.junglenav.data.db.entity.TrackSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineTrackRepository(
    private val trackDao: TrackDao,
) : TrackRepository {
    override fun observeSessions(): Flow<List<TrackSession>> {
        return trackDao.observeSessions().map { entities ->
            entities.map(TrackSessionEntity::toModel)
        }
    }

    override suspend fun saveSession(session: TrackSession) {
        trackDao.upsertSession(session.toEntity())
    }

    override suspend fun appendPoint(sessionId: String, point: TrackPoint) {
        trackDao.insertPoint(point.toEntity(sessionId))
    }
}

private fun TrackSessionEntity.toModel(): TrackSession {
    return TrackSession(
        id = id,
        name = name,
        startedAtEpochMs = startedAtEpochMs,
        endedAtEpochMs = endedAtEpochMs,
        totalDistanceMeters = totalDistanceMeters,
        totalDurationMs = totalDurationMs,
        status = status,
    )
}

private fun TrackSession.toEntity(): TrackSessionEntity {
    return TrackSessionEntity(
        id = id,
        name = name,
        startedAtEpochMs = startedAtEpochMs,
        endedAtEpochMs = endedAtEpochMs,
        totalDistanceMeters = totalDistanceMeters,
        totalDurationMs = totalDurationMs,
        status = status,
    )
}

private fun TrackPoint.toEntity(sessionId: String): TrackPointEntity {
    return TrackPointEntity(
        trackId = sessionId,
        recordedAtEpochMs = recordedAtEpochMs,
        latitude = latitude,
        longitude = longitude,
        altitudeMeters = altitudeMeters,
        headingDegrees = headingDegrees,
        speedMps = speedMps,
        mode = positionMode.name,
        confidence = confidence,
    )
}
