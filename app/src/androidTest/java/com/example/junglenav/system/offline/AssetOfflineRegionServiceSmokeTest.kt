package com.example.junglenav.system.offline

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.junglenav.core.model.OfflineRegionCatalogItem
import java.io.File
import java.net.URI
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssetOfflineRegionServiceSmokeTest {
    @Test
    fun assetPackCanBeDownloadedAndDeleted() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = AssetOfflineRegionService(context)
        val catalogItem = OfflineRegionCatalogItem(
            id = "jakarta-core",
            name = "Jakarta Core",
            summary = "Asset-backed smoke region.",
            styleUri = "asset-pack",
            estimatedSizeBytes = 1_000_000L,
            northLatitude = -6.15,
            eastLongitude = 106.90,
            southLatitude = -6.30,
            westLongitude = 106.75,
            centerLatitude = -6.20,
            centerLongitude = 106.82,
            minZoom = 10.0,
            maxZoom = 12.0,
        )

        val mapPackage = service.downloadRegion(catalogItem) { }
        val stylePath = File(URI(mapPackage.filePath)).absolutePath

        assertTrue(mapPackage.isDownloaded)
        assertNotNull(mapPackage.filePath)
        assertTrue(File(stylePath).exists())

        service.deleteRegion(mapPackage)

        assertFalse(File(stylePath).exists())
    }
}
