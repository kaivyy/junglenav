package com.example.junglenav.system.offline.tiles

data class MbtilesMetadata(
    val format: String,
    val minZoom: Int? = null,
    val maxZoom: Int? = null,
    val bounds: String? = null,
    val center: String? = null,
)

data class MbtilesTile(
    val bytes: ByteArray,
    val contentType: String,
    val format: String,
)
