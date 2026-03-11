package com.example.junglenav.data.repository

import com.example.junglenav.core.model.TrackPoint
import com.example.junglenav.core.model.TrackSession
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun observeSessions(): Flow<List<TrackSession>>
    suspend fun saveSession(session: TrackSession)
    suspend fun appendPoint(sessionId: String, point: TrackPoint)
}
