package com.example.junglenav.system.offline.tiles

import android.content.Context
import java.io.File

class JnavTileServerRegistry(
    private val context: Context,
    private val mbtilesReader: MbtilesReader = MbtilesReader(),
) {
    private var server: JnavTileServer? = null

    @Synchronized
    fun registerPack(
        packId: String,
        installRoot: File,
    ): String {
        val activeServer = server ?: JnavTileServer(
            context = context,
            mbtilesReader = mbtilesReader,
        ).also {
            it.start()
            server = it
        }

        activeServer.registerPack(
            packId = packId,
            installRoot = installRoot,
        )
        return activeServer.baseUrl()
    }

    @Synchronized
    fun baseUrlOrNull(): String? = server?.baseUrl()

    @Synchronized
    fun stop() {
        server?.stop()
        server = null
    }
}
