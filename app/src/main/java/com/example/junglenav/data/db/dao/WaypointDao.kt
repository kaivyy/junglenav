package com.example.junglenav.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.junglenav.data.db.entity.WaypointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<WaypointEntity>>

    @Query("SELECT * FROM waypoints ORDER BY createdAtEpochMs DESC")
    suspend fun observeAllOnce(): List<WaypointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WaypointEntity)

    @Query("DELETE FROM waypoints WHERE id = :id")
    suspend fun delete(id: String)
}
