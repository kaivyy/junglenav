package com.example.junglenav.system.offline.style

import com.example.junglenav.system.offline.jnavpack.JnavPackManifest
import java.io.File

data class ResolvedJnavStyle(
    val styleFile: File,
    val styleJson: String,
) {
    val styleUri: String
        get() = styleFile.toURI().toString()
}

class JnavStyleResolver(
    private val outputRoot: File,
) {
    fun resolve(
        packId: String,
        installRoot: File,
        manifest: JnavPackManifest,
        serverBaseUrl: String,
    ): ResolvedJnavStyle {
        outputRoot.mkdirs()
        val styleFile = File(installRoot, manifest.stylePath)
        val resolvedJson = styleFile.readText()
            .replace("__VECTOR_TILES__", "$serverBaseUrl/packs/$packId/vector/{z}/{x}/{y}.pbf")
            .replace("__HILLSHADE_TILES__", "$serverBaseUrl/packs/$packId/hillshade/{z}/{x}/{y}.png")
            .replace("__IMAGERY_TILES__", "$serverBaseUrl/packs/$packId/imagery/{z}/{x}/{y}.jpg")
            .replace("__GLYPHS_URL__", "$serverBaseUrl/packs/$packId/glyphs/{fontstack}/{range}.pbf")
            .replace("__SPRITES_URL__", "$serverBaseUrl/packs/$packId/sprites/sprite")

        val resolvedStyleFile = File(outputRoot, "$packId-style.json").apply {
            parentFile?.mkdirs()
            writeText(resolvedJson)
        }
        return ResolvedJnavStyle(
            styleFile = resolvedStyleFile,
            styleJson = resolvedJson,
        )
    }
}
