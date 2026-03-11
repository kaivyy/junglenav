package com.example.junglenav.system.offline.catalog

import android.content.Context
import com.example.junglenav.core.model.MapPackSource
import com.example.junglenav.core.model.MapPackage
import com.example.junglenav.core.model.RemoteMapPackCatalogItem
import com.example.junglenav.system.offline.OfflineRegionDownloadProgress
import com.example.junglenav.system.offline.jnavpack.JnavPackInstaller
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface MapPackDownloadService {
    suspend fun download(
        catalogItem: RemoteMapPackCatalogItem,
        onProgress: (OfflineRegionDownloadProgress) -> Unit,
    ): MapPackage

    suspend fun delete(mapPackage: MapPackage)
}

class DefaultMapPackDownloadService(
    context: Context,
    private val installer: JnavPackInstaller,
) : MapPackDownloadService {
    private val appContext = context.applicationContext

    override suspend fun download(
        catalogItem: RemoteMapPackCatalogItem,
        onProgress: (OfflineRegionDownloadProgress) -> Unit,
    ): MapPackage = withContext(Dispatchers.IO) {
        val downloadsDirectory = File(appContext.cacheDir, "jnavpack-downloads").apply { mkdirs() }
        val archiveFile = File(downloadsDirectory, "${catalogItem.id}.jnavpack")

        openInputStream(catalogItem.downloadUrl).use { input ->
            copyToFile(input, archiveFile, catalogItem.estimatedSizeBytes, onProgress)
        }

        installer.install(
            archiveFile = archiveFile,
            source = MapPackSource.REMOTE_CATALOG,
        ).mapPackage
    }

    override suspend fun delete(mapPackage: MapPackage) = withContext(Dispatchers.IO) {
        mapPackage.installRootPath?.let(::File)?.takeIf(File::exists)?.deleteRecursively()
        Unit
    }

    private fun openInputStream(downloadUrl: String): InputStream {
        return when {
            downloadUrl.startsWith("asset://", ignoreCase = true) -> {
                val assetPath = downloadUrl.removePrefix("asset://").trimStart('/')
                appContext.assets.open(assetPath)
            }

            downloadUrl.startsWith("file:", ignoreCase = true) -> {
                File(URI(downloadUrl)).inputStream()
            }

            downloadUrl.startsWith("http://", ignoreCase = true) ||
                downloadUrl.startsWith("https://", ignoreCase = true) -> {
                val connection = URL(downloadUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.inputStream
            }

            else -> File(downloadUrl).inputStream()
        }
    }

    private fun copyToFile(
        input: InputStream,
        archiveFile: File,
        expectedBytes: Long,
        onProgress: (OfflineRegionDownloadProgress) -> Unit,
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var completedBytes = 0L
        val requiredBytes = expectedBytes.coerceAtLeast(1L)

        archiveFile.outputStream().buffered().use { output ->
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                output.write(buffer, 0, read)
                completedBytes += read
                onProgress(
                    OfflineRegionDownloadProgress(
                        completedResources = completedBytes,
                        requiredResources = requiredBytes,
                        completedBytes = completedBytes,
                    ),
                )
            }
        }

        onProgress(
            OfflineRegionDownloadProgress(
                completedResources = requiredBytes,
                requiredResources = requiredBytes,
                completedBytes = archiveFile.length(),
            ),
        )
    }
}
