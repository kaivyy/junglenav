package com.example.junglenav.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "track_points",
    foreignKeys = [
        ForeignKey(
            entity = TrackSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["trackId"])],
)
data class TrackPointEntity(
    @PrimaryKey(autoGenerate = true) val pointId: Long = 0L,
    val trackId: String,
    val recordedAtEpochMs: Long,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val headingDegrees: Double?,
    val speedMps: Double?,
    val mode: String,
    val confidence: Int,
)
