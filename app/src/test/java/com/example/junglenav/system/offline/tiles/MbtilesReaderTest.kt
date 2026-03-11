package com.example.junglenav.system.offline.tiles

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MbtilesReaderTest {
    @Test
    fun readerReturnsCompressedVectorTileForXyzRequest() {
        val backend = FakeMbtilesQueryBackend(
            metadata = mapOf("format" to "pbf"),
            tileData = byteArrayOf(0x1, 0x2, 0x3),
        )
        val reader = MbtilesReader(backend = backend)

        val tile = reader.readTile(
            archivePath = "sample.mbtiles",
            z = 12,
            x = 3351,
            y = 2079,
        )

        assertNotNull(tile)
        assertEquals("application/vnd.mapbox-vector-tile", tile!!.contentType)
        assertEquals(2016, backend.requestedTile?.tmsY)
    }
}

private class FakeMbtilesQueryBackend(
    private val metadata: Map<String, String>,
    private val tileData: ByteArray?,
) : MbtilesQueryBackend {
    var requestedTile: RequestedTile? = null

    override fun <T> open(archivePath: String, block: MbtilesQuerySession.() -> T): T {
        val session = object : MbtilesQuerySession {
            override fun metadata(): Map<String, String> = metadata

            override fun readTile(z: Int, x: Int, tmsY: Int): ByteArray? {
                requestedTile = RequestedTile(z = z, x = x, tmsY = tmsY)
                return tileData
            }
        }
        return session.block()
    }
}

private data class RequestedTile(
    val z: Int,
    val x: Int,
    val tmsY: Int,
)
