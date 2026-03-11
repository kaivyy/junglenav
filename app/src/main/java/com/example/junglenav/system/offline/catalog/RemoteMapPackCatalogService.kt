package com.example.junglenav.system.offline.catalog

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface RemoteMapPackCatalogService {
    suspend fun fetchCatalog(): List<com.example.junglenav.core.model.RemoteMapPackCatalogItem>
}

class DefaultRemoteMapPackCatalogService(
    context: Context,
    private val parser: RemoteMapPackCatalogParser,
    private val catalogEndpoint: String,
) : RemoteMapPackCatalogService {
    private val appContext = context.applicationContext

    override suspend fun fetchCatalog(): List<com.example.junglenav.core.model.RemoteMapPackCatalogItem> =
        withContext(Dispatchers.IO) {
            parser.parse(readText(catalogEndpoint))
        }

    private fun readText(source: String): String {
        return when {
            source.startsWith("asset://", ignoreCase = true) -> {
                val assetPath = source.removePrefix("asset://").trimStart('/')
                appContext.assets.open(assetPath).bufferedReader().use { it.readText() }
            }

            source.startsWith("file:", ignoreCase = true) -> {
                File(URI(source)).readText()
            }

            source.startsWith("http://", ignoreCase = true) ||
                source.startsWith("https://", ignoreCase = true) -> {
                val connection = URL(source).openConnection() as HttpURLConnection
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.inputStream.bufferedReader().use { it.readText() }
            }

            else -> File(source).readText()
        }
    }
}

class SeededRemoteMapPackCatalogService(
    private val parser: RemoteMapPackCatalogParser,
    private val catalogFileProvider: () -> File,
) : RemoteMapPackCatalogService {
    override suspend fun fetchCatalog(): List<com.example.junglenav.core.model.RemoteMapPackCatalogItem> =
        withContext(Dispatchers.IO) {
            parser.parse(catalogFileProvider().readText())
        }
}
