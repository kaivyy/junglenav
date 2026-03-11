package com.example.junglenav.system.offline.tiles

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JnavTileServerSmokeTest {
    @Test
    fun tileServerServesVectorTilesFromRegisteredPack() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packRoot = File(context.cacheDir, "tile-server-smoke-pack").apply {
            deleteRecursively()
            mkdirs()
        }
        val tilesDirectory = File(packRoot, "tiles").apply { mkdirs() }
        createSampleMbtiles(
            file = File(tilesDirectory, "vector.mbtiles"),
            format = "pbf",
            z = 12,
            x = 3351,
            y = 2079,
            data = byteArrayOf(0x1, 0x2, 0x3),
        )

        val server = JnavTileServer(
            context = context,
            mbtilesReader = MbtilesReader(),
        )
        server.registerPack(
            packId = "mission-a",
            installRoot = packRoot,
        )
        server.start()

        val response = URL("${server.baseUrl()}/packs/mission-a/vector/12/3351/2079.pbf")
            .openConnection() as HttpURLConnection

        response.connect()

        assertEquals(200, response.responseCode)
        assertEquals("application/vnd.mapbox-vector-tile", response.contentType)
        assertTrue(response.inputStream.readBytes().isNotEmpty())

        server.stop()
    }
}

private fun createSampleMbtiles(
    file: File,
    format: String,
    z: Int,
    x: Int,
    y: Int,
    data: ByteArray,
) {
    if (file.exists()) {
        file.delete()
    }

    val database = SQLiteDatabase.openOrCreateDatabase(file, null)
    database.execSQL("CREATE TABLE metadata (name TEXT, value TEXT)")
    database.execSQL(
        """
        CREATE TABLE tiles (
            zoom_level INTEGER,
            tile_column INTEGER,
            tile_row INTEGER,
            tile_data BLOB
        )
        """.trimIndent(),
    )
    database.execSQL("INSERT INTO metadata(name, value) VALUES ('format', ?)", arrayOf(format))
    database.execSQL("INSERT INTO metadata(name, value) VALUES ('minzoom', '0')")
    database.execSQL("INSERT INTO metadata(name, value) VALUES ('maxzoom', '16')")
    database.execSQL(
        "INSERT INTO tiles(zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)",
        arrayOf(z, x, xyzToTms(z, y), data),
    )
    database.close()
}

private fun xyzToTms(z: Int, y: Int): Int = ((1 shl z) - 1) - y
