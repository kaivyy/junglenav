package com.example.junglenav.system.offline.jnavpack

import com.example.junglenav.core.model.MapPackSource
import com.example.junglenav.core.model.MapPackTrust
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.io.path.createTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JnavPackInstallerTest {
    @Test
    fun installerMarksUnsignedBundleAsUnverifiedAndRequiresActivationConfirmation() = runTest {
        val workingRoot = createTempDirectory("jnavpack-installer-test").toFile()
        val archiveFile = File(workingRoot, "mission-a.jnavpack")
        createArchive(archiveFile)
        val installer = JnavPackInstaller(
            extractor = JnavPackArchiveExtractor(),
            parser = JnavPackManifestParser(),
            validator = JnavPackValidator(),
            trustResolver = JnavPackTrustResolver(),
            installRoot = File(workingRoot, "installed-packs"),
        )

        val result = installer.install(
            archiveFile = archiveFile,
            source = MapPackSource.LOCAL_IMPORT,
        )

        assertEquals(MapPackTrust.UNVERIFIED, result.mapPackage.trust)
        assertTrue(result.mapPackage.requiresActivationConfirmation)
        assertEquals(MapPackSource.LOCAL_IMPORT, result.mapPackage.source)
        assertNotNull(result.mapPackage.manifestPath)
        assertTrue(result.mapPackage.manifestPath!!.toString().contains("manifest.json"))
        assertTrue(result.mapPackage.filePath.toString().contains("style.json"))
    }
}

private fun createArchive(target: File) {
    ZipOutputStream(target.outputStream().buffered()).use { zip ->
        zip.putNextEntry(ZipEntry("manifest.json"))
        zip.write(
            """
                {
                  "id": "mission-a",
                  "name": "Mission A",
                  "version": "2026.03.11",
                  "packageFormat": 1,
                  "bounds": [-106.98, -6.84, 107.01, -6.71],
                  "center": [106.91, -6.77],
                  "minZoom": 9,
                  "maxZoom": 16,
                  "layers": {
                    "topoVector": true,
                    "hillshadeRaster": true,
                    "imageryRaster": false
                  },
                  "tiles": {
                    "vector": "tiles/vector.mbtiles",
                    "hillshade": "tiles/hillshade.mbtiles"
                  },
                  "stylePath": "style/style.json",
                  "publisher": "Field Team",
                  "signature": null,
                  "checksumSha256": "abc123"
                }
            """.trimIndent().toByteArray(),
        )
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("style/style.json"))
        zip.write("""{"version":8,"sources":{},"layers":[]}""".toByteArray())
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("tiles/vector.mbtiles"))
        zip.write("vector".toByteArray())
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("tiles/hillshade.mbtiles"))
        zip.write("hillshade".toByteArray())
        zip.closeEntry()
    }
}
