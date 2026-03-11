package com.example.junglenav.system.offline.imports

import android.content.Context
import android.net.Uri
import com.example.junglenav.core.model.MapPackSource
import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.system.offline.jnavpack.JnavPackInstaller
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMapPackImportService(
    context: Context,
    private val installer: JnavPackInstaller,
) : MapPackImportService {
    private val appContext = context.applicationContext

    override suspend fun import(source: ImportedMapPackSource): MapPackage = withContext(Dispatchers.IO) {
        val uri = Uri.parse(source.uriString)
        val importsDirectory = File(appContext.cacheDir, "jnavpack-imports").apply { mkdirs() }
        val archiveName = (source.displayName ?: uri.lastPathSegment ?: "imported-pack.jnavpack")
            .sanitizeArchiveName()
        val archiveFile = File(importsDirectory, archiveName)

        appContext.contentResolver.openInputStream(uri)?.use { input ->
            archiveFile.outputStream().buffered().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open import source")

        installer.install(
            archiveFile = archiveFile,
            source = MapPackSource.LOCAL_IMPORT,
        ).mapPackage
    }
}

private fun String.sanitizeArchiveName(): String {
    return replace(Regex("[^A-Za-z0-9._-]"), "_")
}
