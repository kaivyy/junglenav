package com.example.junglenav.feature.map

import com.example.junglenav.core.model.ConfidenceLevel
import com.example.junglenav.core.model.MapPackLayerSet
import com.example.junglenav.core.model.MapPackSource
import com.example.junglenav.core.model.MapPackTrust
import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.core.model.PositionMode
import com.example.junglenav.engine.positioning.NavigationSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapViewModelTest {
    @Test
    fun statusBarUsesLowConfidenceWarningWhenDriftGrows() {
        val viewModel = MapViewModel()

        viewModel.onPositionStateChanged(
            mode = PositionMode.DR_LOW_CONF,
            confidenceLevel = ConfidenceLevel.LOW,
        )

        assertEquals("Low confidence", viewModel.uiState.value.confidenceLabel)
    }

    @Test
    fun invalidOfflinePackageFallsBackToDefaultStyle() {
        val viewModel = MapViewModel()

        viewModel.onActivePackageChanged(
            activePackageName = "Offline Region 1",
            activePackagePath = "offline-region-1.mbtiles",
            isOfflinePackageReady = false,
        )

        assertEquals(MapViewModel.DEFAULT_STYLE_URI, viewModel.uiState.value.styleUri)
        assertTrue(viewModel.uiState.value.isUsingFallbackStyle)
        assertEquals("Live fallback style", viewModel.uiState.value.mapSourceLabel)
    }

    @Test
    fun failedPackageStyleSwitchesToFallbackStyle() {
        val viewModel = MapViewModel()

        viewModel.onActivePackageChanged(
            activePackageName = "Topo Pack",
            activePackagePath = "https://maps.example.com/topo/style.json",
            isOfflinePackageReady = false,
        )
        viewModel.onMapStyleLoadFailed("Style download failed")

        assertEquals(MapViewModel.DEFAULT_STYLE_URI, viewModel.uiState.value.styleUri)
        assertTrue(viewModel.uiState.value.isUsingFallbackStyle)
        assertFalse(viewModel.uiState.value.isMapReady)
        assertEquals("Style download failed", viewModel.uiState.value.mapLoadError)
    }

    @Test
    fun successfulStyleLoadMarksMapReady() {
        val viewModel = MapViewModel()

        viewModel.onMapStyleLoaded()

        assertTrue(viewModel.uiState.value.isMapReady)
        assertEquals(null, viewModel.uiState.value.mapLoadError)
    }

    @Test
    fun gpsFixUpdatesCenterStateAndSourceLabel() {
        val viewModel = MapViewModel()

        viewModel.onLiveGpsFix(
            latitude = -6.21,
            longitude = 106.81,
            source = NavigationSource.GNSS,
            confidence = 82,
        )

        assertTrue(viewModel.uiState.value.hasGpsFix)
        assertEquals("GNSS", viewModel.uiState.value.navigationSourceLabel)
    }

    @Test
    fun offlineRegionDetailsCanBeCollapsed() {
        val viewModel = MapViewModel()

        assertFalse(viewModel.uiState.value.isOfflineRegionExpanded)
    }

    @Test
    fun gpsFollowModeCanBeToggled() {
        val viewModel = MapViewModel()

        viewModel.setGpsFollowing(true)

        assertTrue(viewModel.uiState.value.isFollowingGps)
    }

    @Test
    fun activeOfflinePackageUsesOfflineLabelEvenWhenStyleMatchesDefault() {
        val viewModel = MapViewModel()

        viewModel.onActivePackageChanged(
            activePackageName = "Jakarta Core",
            activePackagePath = MapViewModel.DEFAULT_STYLE_URI,
            isOfflinePackageReady = true,
        )

        assertEquals("Offline region cache", viewModel.uiState.value.mapSourceLabel)
        assertFalse(viewModel.uiState.value.isUsingFallbackStyle)
    }

    @Test
    fun resolvedOfflineBundleUsesJnavpackLabelAndLayerAvailability() {
        val viewModel = MapViewModel()

        viewModel.onOfflineBundleResolved(
            mapPackage = MapPackage(
                id = "mission-a",
                name = "Mission A",
                version = "1",
                sizeBytes = 10_000L,
                isActive = true,
                filePath = "file:///packs/mission-a/style.json",
                checksum = "abc",
                isDownloaded = true,
                source = MapPackSource.REMOTE_CATALOG,
                trust = MapPackTrust.UNVERIFIED,
                layers = MapPackLayerSet(
                    topoVector = true,
                    hillshadeRaster = true,
                    imageryRaster = true,
                ),
            ),
            resolvedStyleUri = "file:///resolved/mission-a-style.json",
        )

        assertEquals("Offline jnavpack", viewModel.uiState.value.mapSourceLabel)
        assertEquals("Unverified", viewModel.uiState.value.activePackTrustLabel)
        assertTrue(viewModel.uiState.value.availableLayers.imageryRaster)
        assertFalse(viewModel.uiState.value.isUsingFallbackStyle)
    }

    @Test
    fun imageryLayerToggleOnlyEnablesWhenImageryLayerExists() {
        val viewModel = MapViewModel()

        viewModel.onOfflineBundleResolved(
            mapPackage = MapPackage(
                id = "mission-b",
                name = "Mission B",
                version = "1",
                sizeBytes = 10_000L,
                isActive = true,
                filePath = "file:///packs/mission-b/style.json",
                checksum = "def",
                isDownloaded = true,
                source = MapPackSource.REMOTE_CATALOG,
                trust = MapPackTrust.VERIFIED,
                layers = MapPackLayerSet(
                    topoVector = true,
                    hillshadeRaster = true,
                    imageryRaster = false,
                ),
            ),
            resolvedStyleUri = "file:///resolved/mission-b-style.json",
        )

        viewModel.setImageryEnabled(true)

        assertFalse(viewModel.uiState.value.canShowImageryToggle)
        assertFalse(viewModel.uiState.value.isImageryEnabled)
    }
}
