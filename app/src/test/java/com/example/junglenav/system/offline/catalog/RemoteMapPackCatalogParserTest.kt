package com.example.junglenav.system.offline.catalog

import com.example.junglenav.core.model.MapPackTrust
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteMapPackCatalogParserTest {
    @Test
    fun parserBuildsCatalogEntriesWithDownloadUrlsAndTrustHints() {
        val sampleJson =
            """
            {
              "generatedAt": "2026-03-11T09:00:00Z",
              "packs": [
                {
                  "id": "gede-a",
                  "name": "Gunung Gede Mission A",
                  "summary": "Topo, hillshade, and imagery bundle for the southern approach.",
                  "downloadUrl": "https://example.com/packs/gede-a.jnavpack",
                  "estimatedSizeBytes": 48000000,
                  "bounds": [-106.98, -6.84, 107.01, -6.71],
                  "center": [106.91, -6.77],
                  "minZoom": 9,
                  "maxZoom": 16,
                  "publisher": "JungleNav Labs",
                  "trustHint": "UNVERIFIED",
                  "layers": {
                    "topoVector": true,
                    "hillshadeRaster": true,
                    "imageryRaster": true
                  }
                }
              ]
            }
            """.trimIndent()

        val entries = RemoteMapPackCatalogParser().parse(sampleJson)

        assertEquals("https://example.com/packs/gede-a.jnavpack", entries.single().downloadUrl)
        assertEquals(MapPackTrust.UNVERIFIED, entries.single().trustHint)
    }
}
