package com.example.junglenav.data.repository

import com.example.junglenav.core.model.MapPackLayerSet
import com.example.junglenav.core.model.MapPackSource
import com.example.junglenav.core.model.MapPackTrust
import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.data.db.dao.MapPackageDao
import com.example.junglenav.data.db.entity.MapPackageEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineMapPackageRepositoryTest {
    @Test
    fun packageRoundTripPreservesTrustSourceAndLayerAvailability() = runTest {
        val repository = OfflineMapPackageRepository(FakeMapPackageDao())
        val mapPackage = MapPackage(
            id = "mission-a",
            name = "Mission A",
            version = "1",
            sizeBytes = 10_000L,
            isActive = false,
            filePath = "file:///packs/mission-a/style.json",
            checksum = "abc",
            isDownloaded = true,
            centerLatitude = -6.77,
            centerLongitude = 106.91,
            source = MapPackSource.REMOTE_CATALOG,
            trust = MapPackTrust.UNVERIFIED,
            layers = MapPackLayerSet(
                topoVector = true,
                hillshadeRaster = true,
                imageryRaster = false,
            ),
        )

        repository.save(mapPackage)

        val saved = repository.observePackages().first().single()
        assertEquals(MapPackSource.REMOTE_CATALOG, saved.source)
        assertEquals(MapPackTrust.UNVERIFIED, saved.trust)
        assertEquals(
            MapPackLayerSet(
                topoVector = true,
                hillshadeRaster = true,
                imageryRaster = false,
            ),
            saved.layers,
        )
    }
}

private class FakeMapPackageDao : MapPackageDao {
    private val state = MutableStateFlow<List<MapPackageEntity>>(emptyList())

    override fun observeAll(): Flow<List<MapPackageEntity>> = state

    override suspend fun upsert(entity: MapPackageEntity) {
        state.value = state.value.filterNot { it.id == entity.id } + entity
    }

    override suspend fun clearActivePackage() {
        state.value = state.value.map { it.copy(isActive = false) }
    }

    override suspend fun markActive(id: String) {
        state.value = state.value.map { it.copy(isActive = it.id == id) }
    }

    override suspend fun delete(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}
