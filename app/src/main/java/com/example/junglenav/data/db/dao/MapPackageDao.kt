package com.example.junglenav.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.junglenav.data.db.entity.MapPackageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapPackageDao {
    @Query("SELECT * FROM map_packages ORDER BY name ASC")
    fun observeAll(): Flow<List<MapPackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MapPackageEntity)

    @Query("UPDATE map_packages SET isActive = 0")
    suspend fun clearActivePackage()

    @Query("UPDATE map_packages SET isActive = 1 WHERE id = :id")
    suspend fun markActive(id: String)

    @Query("DELETE FROM map_packages WHERE id = :id")
    suspend fun delete(id: String)
}
