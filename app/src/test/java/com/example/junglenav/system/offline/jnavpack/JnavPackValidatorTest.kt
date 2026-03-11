package com.example.junglenav.system.offline.jnavpack

import com.example.junglenav.core.model.MapPackLayerSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JnavPackValidatorTest {
    @Test
    fun parserReadsManifestWithOptionalRasterLayers() {
        val manifest = JnavPackManifestParser().parse(
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
                  "publisher": "JungleNav Labs"
                }
            """.trimIndent(),
        )

        assertEquals("mission-a", manifest.id)
        assertEquals("tiles/vector.mbtiles", manifest.vectorTilesPath)
        assertEquals("tiles/hillshade.mbtiles", manifest.hillshadeTilesPath)
        assertEquals(
            MapPackLayerSet(
                topoVector = true,
                hillshadeRaster = true,
                imageryRaster = false,
            ),
            manifest.layers,
        )
    }

    @Test
    fun parserTreatsJsonNullAsMissingForOptionalFields() {
        val manifest = JnavPackManifestParser().parse(
            """
                {
                  "id": "mission-b",
                  "name": "Mission B",
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
                  "publisher": null,
                  "signature": null,
                  "checksumSha256": null
                }
            """.trimIndent(),
        )

        assertEquals(null, manifest.publisher)
        assertEquals(null, manifest.signature)
        assertEquals(null, manifest.checksumSha256)
    }

    @Test
    fun validatorRejectsBundleWithoutVectorTilesOrStyle() {
        val manifest = JnavPackManifest(
            id = "broken-pack",
            name = "Broken Pack",
            version = "1",
            packageFormat = 1,
            bounds = JnavPackBounds(-106.98, -6.84, 107.01, -6.71),
            center = JnavPackCenter(106.91, -6.77),
            minZoom = 9.0,
            maxZoom = 16.0,
            layers = MapPackLayerSet(topoVector = true),
            stylePath = "",
            vectorTilesPath = null,
            hillshadeTilesPath = null,
            imageryTilesPath = null,
            publisher = null,
            signature = null,
            checksumSha256 = null,
        )

        val result = JnavPackValidator().validate(manifest)

        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Missing stylePath"))
        assertTrue(result.errors.contains("Missing vectorTilesPath"))
    }
}
