package com.example.junglenav.system.offline.tiles

data class MbtilesTileRequest(
    val archivePath: String,
    val z: Int,
    val x: Int,
    val y: Int,
) {
    val tmsY: Int
        get() = ((1 shl z) - 1) - y
}
