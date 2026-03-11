package com.example.junglenav.data.repository

import com.example.junglenav.core.model.MapPackage
import kotlinx.coroutines.flow.Flow

interface MapPackageRepository {
    fun observePackages(): Flow<List<MapPackage>>

    suspend fun save(mapPackage: MapPackage)

    suspend fun activate(id: String)

    suspend fun delete(id: String) {}
}
