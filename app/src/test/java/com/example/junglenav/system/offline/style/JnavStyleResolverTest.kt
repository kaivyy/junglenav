package com.example.junglenav.system.offline.style

import com.example.junglenav.core.model.MapPackLayerSet
import com.example.junglenav.system.offline.jnavpack.JnavPackBounds
import com.example.junglenav.system.offline.jnavpack.JnavPackCenter
import com.example.junglenav.system.offline.jnavpack.JnavPackManifest
import java.io.File
import kotlin.io.path.createTempDirectory
import org.junit.Assert.assertTrue
import org.junit.Test

class JnavStyleResolverTest {
    @Test
    fun resolverRewritesBundleStyleToLoopbackTileEndpoints() {
        val workingRoot = createTempDirectory("style-resolver-test").toFile()
        val installRoot = File(workingRoot, "mission-a").apply {
            mkdirs()
        }
        val styleDirectory = File(installRoot, "style").apply { mkdirs() }
        File(styleDirectory, "style.json").writeText(
            """
            {
              "version": 8,
              "sources": {
                "topo": {
                  "type": "vector",
                  "tiles": ["__VECTOR_TILES__"]
                },
                "imagery": {
                  "type": "raster",
                  "tiles": ["__IMAGERY_TILES__"]
                }
              },
              "glyphs": "__GLYPHS_URL__",
              "sprite": "__SPRITES_URL__",
              "layers": []
            }
            """.trimIndent(),
        )

        val manifest = JnavPackManifest(
            id = "mission-a",
            name = "Mission A",
            version = "1",
            packageFormat = 1,
            bounds = JnavPackBounds(106.8, -6.8, 106.9, -6.7),
            center = JnavPackCenter(106.85, -6.75),
            minZoom = 9.0,
            maxZoom = 16.0,
            layers = MapPackLayerSet(topoVector = true, hillshadeRaster = true, imageryRaster = true),
            stylePath = "style/style.json",
            vectorTilesPath = "tiles/vector.mbtiles",
            hillshadeTilesPath = "tiles/hillshade.mbtiles",
            imageryTilesPath = "tiles/imagery.mbtiles",
            publisher = "JungleNav Labs",
            signature = null,
            checksumSha256 = "abc",
        )

        val resolved = JnavStyleResolver(File(workingRoot, "resolved-styles")).resolve(
            packId = "mission-a",
            installRoot = installRoot,
            manifest = manifest,
            serverBaseUrl = "http://127.0.0.1:38479",
        )

        assertTrue(resolved.styleJson.contains("http://127.0.0.1:38479/packs/mission-a/vector"))
        assertTrue(resolved.styleJson.contains("http://127.0.0.1:38479/packs/mission-a/imagery"))
        assertTrue(resolved.styleFile.exists())
    }
}
