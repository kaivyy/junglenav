package com.example.junglenav.system.offline.tiles

import android.database.sqlite.SQLiteDatabase
import java.io.File

class MbtilesReader(
    private val backend: MbtilesQueryBackend = AndroidMbtilesQueryBackend(),
) {
    fun readMetadata(archivePath: String): MbtilesMetadata {
        return backend.open(archivePath) {
            val metadata = metadata()
            MbtilesMetadata(
                format = metadata["format"].orEmpty(),
                minZoom = metadata["minzoom"]?.toIntOrNull(),
                maxZoom = metadata["maxzoom"]?.toIntOrNull(),
                bounds = metadata["bounds"],
                center = metadata["center"],
            )
        }
    }

    fun readTile(
        archivePath: String,
        z: Int,
        x: Int,
        y: Int,
    ): MbtilesTile? {
        val request = MbtilesTileRequest(
            archivePath = archivePath,
            z = z,
            x = x,
            y = y,
        )

        return backend.open(archivePath) {
            val metadata = metadata()
            val tileBytes = readTile(request.z, request.x, request.tmsY) ?: return@open null
            val format = metadata["format"]
                ?.takeIf(String::isNotBlank)
                ?: File(archivePath).extension

            MbtilesTile(
                bytes = tileBytes,
                contentType = format.toContentType(),
                format = format,
            )
        }
    }
}

interface MbtilesQueryBackend {
    fun <T> open(archivePath: String, block: MbtilesQuerySession.() -> T): T
}

interface MbtilesQuerySession {
    fun metadata(): Map<String, String>

    fun readTile(z: Int, x: Int, tmsY: Int): ByteArray?
}

private class AndroidMbtilesQueryBackend : MbtilesQueryBackend {
    override fun <T> open(archivePath: String, block: MbtilesQuerySession.() -> T): T {
        val database = SQLiteDatabase.openDatabase(
            archivePath,
            null,
            SQLiteDatabase.OPEN_READONLY,
        )
        return try {
            val session = object : MbtilesQuerySession {
                override fun metadata(): Map<String, String> {
                    val results = linkedMapOf<String, String>()
                    database.rawQuery("SELECT name, value FROM metadata", null).use { cursor ->
                        while (cursor.moveToNext()) {
                            results[cursor.getString(0)] = cursor.getString(1)
                        }
                    }
                    return results
                }

                override fun readTile(z: Int, x: Int, tmsY: Int): ByteArray? {
                    database.rawQuery(
                        """
                        SELECT tile_data
                        FROM tiles
                        WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?
                        LIMIT 1
                        """.trimIndent(),
                        arrayOf(z.toString(), x.toString(), tmsY.toString()),
                    ).use { cursor ->
                        return if (cursor.moveToFirst()) {
                            cursor.getBlob(0)
                        } else {
                            null
                        }
                    }
                }
            }
            session.block()
        } finally {
            database.close()
        }
    }
}

private fun String.toContentType(): String {
    return when (lowercase()) {
        "pbf", "mvt" -> "application/vnd.mapbox-vector-tile"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
    }
}
