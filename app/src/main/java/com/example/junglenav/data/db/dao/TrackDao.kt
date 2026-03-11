package com.example.junglenav.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.junglenav.data.db.entity.TrackPointEntity
import com.example.junglenav.data.db.entity.TrackSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY startedAtEpochMs DESC")
    fun observeSessions(): Flow<List<TrackSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(entity: TrackSessionEntity)

    @Insert
    suspend fun insertPoint(entity: TrackPointEntity)

    @Query("SELECT * FROM track_points WHERE trackId = :trackId ORDER BY recordedAtEpochMs ASC")
    suspend fun getPoints(trackId: String): List<TrackPointEntity>
}
