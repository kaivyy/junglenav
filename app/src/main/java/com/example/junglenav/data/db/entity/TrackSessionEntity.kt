package com.example.junglenav.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackSessionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long?,
    val totalDistanceMeters: Double = 0.0,
    val totalDurationMs: Long = 0L,
    val status: String,
)
