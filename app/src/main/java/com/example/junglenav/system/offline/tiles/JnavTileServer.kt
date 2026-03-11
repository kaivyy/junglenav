package com.example.junglenav.system.offline.tiles

import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class JnavTileServer(
    context: Context,
    private val mbtilesReader: MbtilesReader,
) {
    private val appContext = context.applicationContext
    private val packRoots = ConcurrentHashMap<String, File>()
    private val running = AtomicBoolean(false)
    private val workerPool: ExecutorService = Executors.newCachedThreadPool()
    @Volatile
    private var serverSocket: ServerSocket? = null
    @Volatile
    private var port: Int = -1

    fun registerPack(
        packId: String,
        installRoot: File,
    ) {
        packRoots[packId] = installRoot
    }

    fun start() {
        if (running.compareAndSet(false, true).not()) return

        val socket = ServerSocket(
            0,
            50,
            InetAddress.getByName("127.0.0.1"),
        )
        serverSocket = socket
        port = socket.localPort

        workerPool.execute {
            while (running.get()) {
                val client = try {
                    socket.accept()
                } catch (_: Exception) {
                    break
                }
                workerPool.execute {
                    client.use(::handleClient)
                }
            }
        }
    }

    fun baseUrl(): String {
        check(port > 0) { "Tile server is not running" }
        return "http://127.0.0.1:$port"
    }

    fun stop() {
        running.set(false)
        serverSocket?.close()
        serverSocket = null
        port = -1
        packRoots.clear()
        workerPool.shutdownNow()
    }

    private fun handleClient(socket: Socket) {
        val requestLine = BufferedReader(
            InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8),
        ).readLine().orEmpty()
        val path = requestLine.split(" ").getOrNull(1)?.substringBefore('?').orEmpty()
        val response = route(path)

        socket.getOutputStream().use { output ->
            val header =
                buildString {
                    append("HTTP/1.1 ${response.statusCode} ${response.reason}\r\n")
                    append("Content-Type: ${response.contentType}\r\n")
                    append("Content-Length: ${response.body.size}\r\n")
                    append("Connection: close\r\n")
                    append("\r\n")
                }
            output.write(header.toByteArray(StandardCharsets.UTF_8))
            output.write(response.body)
            output.flush()
        }
    }

    private fun route(path: String): HttpResponse {
        val segments = path.trim('/').split('/').filter(String::isNotBlank)
        if (segments.size < 4 || segments.firstOrNull() != "packs") {
            return HttpResponse.notFound()
        }

        val packId = segments[1]
        val packRoot = packRoots[packId] ?: return HttpResponse.notFound()

        return when (segments[2]) {
            "vector" -> serveMbtiles(packRoot, "tiles/vector.mbtiles", segments, "pbf")
            "hillshade" -> serveMbtiles(packRoot, "tiles/hillshade.mbtiles", segments, "webp")
            "imagery" -> serveMbtiles(packRoot, "tiles/imagery.mbtiles", segments, "jpg")
            "sprites" -> serveFile(packRoot, segments.drop(2), "application/json")
            "glyphs" -> serveFile(packRoot, segments.drop(2), "application/x-protobuf")
            else -> HttpResponse.notFound()
        }
    }

    private fun serveMbtiles(
        packRoot: File,
        relativeArchivePath: String,
        segments: List<String>,
        defaultExtension: String,
    ): HttpResponse {
        if (segments.size < 6) return HttpResponse.notFound()
        val z = segments[3].toIntOrNull() ?: return HttpResponse.notFound()
        val x = segments[4].toIntOrNull() ?: return HttpResponse.notFound()
        val y = segments[5].substringBefore('.').toIntOrNull() ?: return HttpResponse.notFound()
        val archiveFile = File(packRoot, relativeArchivePath)
        if (!archiveFile.exists()) return HttpResponse.notFound()

        val tile = mbtilesReader.readTile(
            archivePath = archiveFile.absolutePath,
            z = z,
            x = x,
            y = y,
        ) ?: return HttpResponse.notFound()

        val contentType = if (tile.contentType == "application/octet-stream") {
            defaultExtension.toFallbackContentType()
        } else {
            tile.contentType
        }
        return HttpResponse.ok(
            body = tile.bytes,
            contentType = contentType,
        )
    }

    private fun serveFile(
        packRoot: File,
        relativeSegments: List<String>,
        defaultContentType: String,
    ): HttpResponse {
        val file = relativeSegments.fold(packRoot) { current, segment ->
            File(current, segment)
        }
        if (!file.exists() || !file.isFile) return HttpResponse.notFound()
        return HttpResponse.ok(
            body = file.readBytes(),
            contentType = file.extension.toFallbackContentType(defaultContentType),
        )
    }
}

private data class HttpResponse(
    val statusCode: Int,
    val reason: String,
    val contentType: String,
    val body: ByteArray,
) {
    companion object {
        fun ok(body: ByteArray, contentType: String): HttpResponse {
            return HttpResponse(
                statusCode = 200,
                reason = "OK",
                contentType = contentType,
                body = body,
            )
        }

        fun notFound(): HttpResponse {
            return HttpResponse(
                statusCode = 404,
                reason = "Not Found",
                contentType = "text/plain; charset=utf-8",
                body = "Not found".toByteArray(StandardCharsets.UTF_8),
            )
        }
    }
}

private fun String.toFallbackContentType(defaultContentType: String = "application/octet-stream"): String {
    return when (lowercase()) {
        "pbf", "mvt" -> "application/vnd.mapbox-vector-tile"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "webp" -> "image/webp"
        "json" -> "application/json"
        else -> defaultContentType
    }
}
