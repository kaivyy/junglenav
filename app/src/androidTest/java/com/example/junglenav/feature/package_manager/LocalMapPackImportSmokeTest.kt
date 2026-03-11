package com.example.junglenav.feature.package_manager

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.junglenav.core.model.MapPackTrust
import com.example.junglenav.system.offline.imports.ImportedMapPackSource
import com.example.junglenav.system.offline.imports.LocalMapPackImportService
import com.example.junglenav.system.offline.jnavpack.JnavPackArchiveExtractor
import com.example.junglenav.system.offline.jnavpack.JnavPackInstaller
import com.example.junglenav.system.offline.jnavpack.JnavPackManifestParser
import com.example.junglenav.system.offline.jnavpack.JnavPackTrustResolver
import com.example.junglenav.system.offline.jnavpack.JnavPackValidator
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalMapPackImportSmokeTest {
    @Test
    fun importServiceInstallsJnavpackFromFileUri() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val archiveFile = File(context.cacheDir, "local-import-smoke.jnavpack")
        createArchive(archiveFile)
        val service = LocalMapPackImportService(
            context = context,
            installer = JnavPackInstaller(
                extractor = JnavPackArchiveExtractor(),
                parser = JnavPackManifestParser(),
                validator = JnavPackValidator(),
                trustResolver = JnavPackTrustResolver(),
                installRoot = File(context.filesDir, "smoke-installed-packs"),
            ),
        )

        val imported = service.import(
            ImportedMapPackSource(
                uriString = Uri.fromFile(archiveFile).toString(),
                displayName = archiveFile.name,
            ),
        )

        assertEquals(MapPackTrust.UNVERIFIED, imported.trust)
        assertTrue(imported.requiresActivationConfirmation)
        assertTrue(imported.filePath.contains("style.json"))
    }
}

private fun createArchive(target: File) {
    ZipOutputStream(target.outputStream().buffered()).use { zip ->
        zip.putNextEntry(ZipEntry("manifest.json"))
        zip.write(
            """
                {
                  "id": "smoke-pack",
                  "name": "Smoke Pack",
                  "version": "2026.03.11",
                  "packageFormat": 1,
                  "bounds": [-106.98, -6.84, 107.01, -6.71],
                  "center": [106.91, -6.77],
                  "minZoom": 9,
                  "maxZoom": 16,
                  "layers": {
                    "topoVector": true
                  },
                  "tiles": {
                    "vector": "tiles/vector.mbtiles"
                  },
                  "stylePath": "style/style.json",
                  "signature": null
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
    }
}
