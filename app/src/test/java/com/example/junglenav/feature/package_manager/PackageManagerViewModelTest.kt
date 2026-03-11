package com.example.junglenav.feature.package_manager

import com.example.junglenav.core.model.MapPackLayerSet
import com.example.junglenav.core.model.MapPackTrust
import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.core.model.RemoteMapPackCatalogItem
import com.example.junglenav.data.repository.MapPackageRepository
import com.example.junglenav.system.offline.imports.ImportedMapPackSource
import com.example.junglenav.system.offline.imports.MapPackImportService
import com.example.junglenav.system.offline.OfflineRegionDownloadProgress
import com.example.junglenav.system.offline.catalog.MapPackDownloadService
import com.example.junglenav.system.offline.catalog.RemoteMapPackCatalogService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PackageManagerViewModelTest {
    @Test
    fun activatePackageMarksOnlyOnePackageActive() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = PackageManagerViewModel(
            repository = FakeMapPackageRepository(
                initial = listOf(
                    MapPackage(id = "a", name = "Region A", version = "1", sizeBytes = 100L, isActive = false, filePath = "a.bundle", checksum = "1"),
                    MapPackage(id = "b", name = "Region B", version = "1", sizeBytes = 100L, isActive = false, filePath = "b.bundle", checksum = "2"),
                ),
            ),
            catalogService = FakeRemoteMapPackCatalogService(defaultCatalog()),
            downloadService = FakeMapPackDownloadService(),
            importService = FakeMapPackImportService(),
            scope = CoroutineScope(dispatcher),
        )

        advanceUntilIdle()
        viewModel.activate("b")
        advanceUntilIdle()

        assertEquals("b", viewModel.uiState.value.items.single { it.isActive }.id)
    }

    @Test
    fun searchQueryFiltersCatalogItemsByName() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = PackageManagerViewModel(
            repository = FakeMapPackageRepository(initial = emptyList()),
            catalogService = FakeRemoteMapPackCatalogService(defaultCatalog()),
            downloadService = FakeMapPackDownloadService(),
            importService = FakeMapPackImportService(),
            scope = CoroutineScope(dispatcher),
        )

        advanceUntilIdle()
        viewModel.onSearchQueryChanged("Bandung")

        assertEquals(listOf("Bandung Highlands"), viewModel.uiState.value.catalogItems.map { it.name })
    }

    @Test
    fun downloadMissionPackSavesPackageAndTracksProgress() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeMapPackageRepository(initial = emptyList())
        val downloadService = FakeMapPackDownloadService()
        val viewModel = PackageManagerViewModel(
            repository = repository,
            catalogService = FakeRemoteMapPackCatalogService(defaultCatalog()),
            downloadService = downloadService,
            importService = FakeMapPackImportService(),
            scope = CoroutineScope(dispatcher),
        )

        advanceUntilIdle()
        viewModel.downloadCatalogPack("jakarta-core")
        advanceUntilIdle()

        assertEquals("jakarta-core", viewModel.uiState.value.items.single().id)
        assertEquals(100, viewModel.uiState.value.downloadProgressById["jakarta-core"])
        assertTrue(downloadService.requestedIds.contains("jakarta-core"))
    }

    @Test
    fun importBundleAddsInstalledPackageToInventory() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = FakeMapPackageRepository(initial = emptyList())
        val importService = FakeMapPackImportService()
        val viewModel = PackageManagerViewModel(
            repository = repository,
            catalogService = FakeRemoteMapPackCatalogService(defaultCatalog()),
            downloadService = FakeMapPackDownloadService(),
            importService = importService,
            scope = CoroutineScope(dispatcher),
        )

        advanceUntilIdle()
        viewModel.onImportCompleted(
            ImportedMapPackSource(
                uriString = "file:///tmp/gede-a.jnavpack",
                displayName = "gede-a.jnavpack",
            ),
        )
        advanceUntilIdle()

        assertEquals("gunung-gede-mission-a", viewModel.uiState.value.items.single().id)
        assertTrue(importService.requestedSources.single().uriString.contains("gede-a.jnavpack"))
    }

    @Test
    fun unverifiedPackageRequiresConfirmationBeforeActivation() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = PackageManagerViewModel(
            repository = FakeMapPackageRepository(
                initial = listOf(
                    MapPackage(
                        id = "unverified-pack",
                        name = "Unverified Pack",
                        version = "1",
                        sizeBytes = 100L,
                        isActive = false,
                        filePath = "file:///packs/unverified/style.json",
                        checksum = "unsafe-001",
                        isDownloaded = true,
                        trust = MapPackTrust.UNVERIFIED,
                        publisher = "Unknown Publisher",
                        requiresActivationConfirmation = true,
                    ),
                ),
            ),
            catalogService = FakeRemoteMapPackCatalogService(defaultCatalog()),
            downloadService = FakeMapPackDownloadService(),
            importService = FakeMapPackImportService(),
            scope = CoroutineScope(dispatcher),
        )

        advanceUntilIdle()
        viewModel.activate("unverified-pack")
        advanceUntilIdle()

        assertEquals("unverified-pack", viewModel.uiState.value.pendingActivation?.packageId)
        assertEquals(null, viewModel.uiState.value.activePackage)
    }
}

private class FakeMapPackageRepository(
    initial: List<MapPackage>,
) : MapPackageRepository {
    private val state = MutableStateFlow(initial)

    override fun observePackages(): Flow<List<MapPackage>> = state

    override suspend fun save(mapPackage: MapPackage) {
        state.value = state.value.filterNot { it.id == mapPackage.id } + mapPackage
    }

    override suspend fun activate(id: String) {
        state.value = state.value.map { item -> item.copy(isActive = item.id == id) }
    }

    override suspend fun delete(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}

private class FakeRemoteMapPackCatalogService(
    private val items: List<RemoteMapPackCatalogItem>,
) : RemoteMapPackCatalogService {
    override suspend fun fetchCatalog(): List<RemoteMapPackCatalogItem> = items
}

private class FakeMapPackDownloadService : MapPackDownloadService {
    val requestedIds = mutableListOf<String>()

    override suspend fun download(
        catalogItem: RemoteMapPackCatalogItem,
        onProgress: (OfflineRegionDownloadProgress) -> Unit,
    ): MapPackage {
        requestedIds += catalogItem.id
        onProgress(OfflineRegionDownloadProgress(completedResources = 5, requiredResources = 10, completedBytes = 512L))
        onProgress(OfflineRegionDownloadProgress(completedResources = 10, requiredResources = 10, completedBytes = 1_024L))
        return MapPackage(
            id = catalogItem.id,
            name = catalogItem.name,
            version = "1.0",
            sizeBytes = catalogItem.estimatedSizeBytes,
            isActive = false,
            filePath = "file:///packs/${catalogItem.id}/style.json",
            checksum = "offline-${catalogItem.id}",
            isDownloaded = true,
            offlineRegionId = 42L,
            centerLatitude = catalogItem.centerLatitude,
            centerLongitude = catalogItem.centerLongitude,
        )
    }

    override suspend fun delete(mapPackage: MapPackage) = Unit
}

private class FakeMapPackImportService : MapPackImportService {
    val requestedSources = mutableListOf<ImportedMapPackSource>()

    override suspend fun import(source: ImportedMapPackSource): MapPackage {
        requestedSources += source
        return MapPackage(
            id = "gunung-gede-mission-a",
            name = "Gunung Gede Mission A",
            version = "2026.03.11",
            sizeBytes = 25_000_000L,
            isActive = false,
            filePath = "file:///packs/gede/style.json",
            checksum = "imported-pack",
            isDownloaded = true,
            centerLatitude = -6.77,
            centerLongitude = 106.91,
        )
    }
}

private fun defaultCatalog(): List<RemoteMapPackCatalogItem> {
    return listOf(
        RemoteMapPackCatalogItem(
            id = "jakarta-core",
            name = "Jakarta Core",
            summary = "Urban core region for offline field testing.",
            downloadUrl = "https://example.com/packs/jakarta-core.jnavpack",
            estimatedSizeBytes = 18_000_000L,
            northLatitude = -6.15,
            eastLongitude = 106.90,
            southLatitude = -6.30,
            westLongitude = 106.75,
            centerLatitude = -6.20,
            centerLongitude = 106.82,
            minZoom = 10.0,
            maxZoom = 12.0,
            publisher = "JungleNav Labs",
            trustHint = MapPackTrust.VERIFIED,
            layers = MapPackLayerSet(topoVector = true, hillshadeRaster = true, imageryRaster = true),
        ),
        RemoteMapPackCatalogItem(
            id = "bandung-highlands",
            name = "Bandung Highlands",
            summary = "Hill route pack for denser contour testing.",
            downloadUrl = "https://example.com/packs/bandung-highlands.jnavpack",
            estimatedSizeBytes = 22_000_000L,
            northLatitude = -6.78,
            eastLongitude = 107.74,
            southLatitude = -6.99,
            westLongitude = 107.50,
            centerLatitude = -6.91,
            centerLongitude = 107.61,
            minZoom = 10.0,
            maxZoom = 12.0,
            publisher = "JungleNav Labs",
            trustHint = MapPackTrust.UNVERIFIED,
            layers = MapPackLayerSet(topoVector = true, hillshadeRaster = true, imageryRaster = false),
        ),
    )
}
