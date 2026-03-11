package com.example.junglenav.data.repository

import com.example.junglenav.core.model.MapPackLayerSet
import com.example.junglenav.core.model.MapPackSource
import com.example.junglenav.core.model.MapPackTrust
import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.data.db.dao.MapPackageDao
import com.example.junglenav.data.db.entity.MapPackageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineMapPackageRepository(
    private val dao: MapPackageDao,
) : MapPackageRepository {
    override fun observePackages(): Flow<List<MapPackage>> {
        return dao.observeAll().map { entities ->
            entities.map(MapPackageEntity::toModel)
        }
    }

    override suspend fun save(mapPackage: MapPackage) {
        dao.upsert(mapPackage.toEntity())
    }

    override suspend fun activate(id: String) {
        dao.clearActivePackage()
        dao.markActive(id)
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}

private fun MapPackageEntity.toModel(): MapPackage {
    return MapPackage(
        id = id,
        name = name,
        version = version,
        sizeBytes = sizeBytes,
        isActive = isActive,
        filePath = filePath,
        checksum = checksum,
        isDownloaded = isDownloaded,
        offlineRegionId = offlineRegionId,
        centerLatitude = centerLatitude,
        centerLongitude = centerLongitude,
        installRootPath = installRootPath,
        manifestPath = manifestPath,
        source = source.toMapPackSource(),
        trust = trust.toMapPackTrust(),
        publisher = publisher,
        requiresActivationConfirmation = requiresActivationConfirmation,
        layers = MapPackLayerSet(
            topoVector = hasTopoVector,
            hillshadeRaster = hasHillshadeRaster,
            imageryRaster = hasImageryRaster,
        ),
        thumbnailPath = thumbnailPath,
    )
}

private fun MapPackage.toEntity(): MapPackageEntity {
    return MapPackageEntity(
        id = id,
        name = name,
        version = version,
        sizeBytes = sizeBytes,
        filePath = filePath,
        checksum = checksum,
        isActive = isActive,
        isDownloaded = isDownloaded,
        offlineRegionId = offlineRegionId,
        centerLatitude = centerLatitude,
        centerLongitude = centerLongitude,
        installRootPath = installRootPath,
        manifestPath = manifestPath,
        source = source.name,
        trust = trust.name,
        publisher = publisher,
        requiresActivationConfirmation = requiresActivationConfirmation,
        hasTopoVector = layers.topoVector,
        hasHillshadeRaster = layers.hillshadeRaster,
        hasImageryRaster = layers.imageryRaster,
        thumbnailPath = thumbnailPath,
    )
}

private fun String.toMapPackSource(): MapPackSource {
    return runCatching { MapPackSource.valueOf(this) }
        .getOrDefault(MapPackSource.BUNDLED_ASSET)
}

private fun String.toMapPackTrust(): MapPackTrust {
    return runCatching { MapPackTrust.valueOf(this) }
        .getOrDefault(MapPackTrust.VERIFIED)
}
