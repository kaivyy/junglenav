package com.example.junglenav.system.offline.demo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DemoMissionPackSeeder(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val rootDirectory = File(appContext.filesDir, "demo-mission-packs")

    @Synchronized
    fun ensureCatalogFile(): File {
        rootDirectory.mkdirs()
        val descriptors = demoDescriptors()
        descriptors.forEach(::ensureBundleArchive)
        val catalogFile = File(rootDirectory, "catalog.json")
        catalogFile.writeText(buildCatalogJson(descriptors))
        return catalogFile
    }

    private fun ensureBundleArchive(descriptor: DemoMissionDescriptor): File {
        val archiveFile = File(rootDirectory, "${descriptor.id}.jnavpack")
        if (archiveFile.exists()) {
            return archiveFile
        }

        val bundleRoot = File(rootDirectory, "${descriptor.id}-bundle-${System.nanoTime()}").apply { mkdirs() }
        val styleDirectory = File(bundleRoot, "style").apply { mkdirs() }
        val tilesDirectory = File(bundleRoot, "tiles").apply { mkdirs() }
        val previewDirectory = File(bundleRoot, "preview").apply { mkdirs() }
        val tempArchive = File(rootDirectory, "${descriptor.id}.jnavpack.part").apply {
            if (exists()) {
                delete()
            }
        }

        File(styleDirectory, "style.json").writeText(buildStyleJson(descriptor))
        File(previewDirectory, "thumbnail.png").writeBytes(buildPreviewPng(descriptor))

        createMbtilesArchive(
            file = File(tilesDirectory, "vector.mbtiles"),
            format = "pbf",
            descriptor = descriptor,
            tileBytes = DemoVectorTileEncoder.buildMissionTile(),
        )
        createMbtilesArchive(
            file = File(tilesDirectory, "hillshade.mbtiles"),
            format = "png",
            descriptor = descriptor,
            tileBytes = buildHillshadePng(descriptor),
        )
        createMbtilesArchive(
            file = File(tilesDirectory, "imagery.mbtiles"),
            format = "jpg",
            descriptor = descriptor,
            tileBytes = buildImageryJpeg(descriptor),
        )

        File(bundleRoot, "manifest.json").writeText(buildManifestJson(descriptor))
        zipDirectory(bundleRoot, tempArchive)
        if (!tempArchive.renameTo(archiveFile)) {
            tempArchive.copyTo(archiveFile, overwrite = true)
            tempArchive.delete()
        }
        bundleRoot.deleteRecursively()
        return archiveFile
    }

    private fun createMbtilesArchive(
        file: File,
        format: String,
        descriptor: DemoMissionDescriptor,
        tileBytes: ByteArray,
    ) {
        if (file.exists()) {
            SQLiteDatabase.deleteDatabase(file)
        }

        val database = SQLiteDatabase.openDatabase(
            file.absolutePath,
            null,
            SQLiteDatabase.CREATE_IF_NECESSARY or SQLiteDatabase.OPEN_READWRITE,
        )
        try {
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
            database.execSQL("CREATE UNIQUE INDEX tile_index ON tiles (zoom_level, tile_column, tile_row)")

            val boundsValue =
                "${descriptor.westLongitude},${descriptor.southLatitude},${descriptor.eastLongitude},${descriptor.northLatitude}"
            val centerValue = "${descriptor.centerLongitude},${descriptor.centerLatitude},12"
            insertMetadata(database, "name", descriptor.name)
            insertMetadata(database, "format", format)
            insertMetadata(database, "minzoom", "9")
            insertMetadata(database, "maxzoom", "13")
            insertMetadata(database, "bounds", boundsValue)
            insertMetadata(database, "center", centerValue)
            insertMetadata(database, "type", "overlay")

            for (z in 9..13) {
                val (centerX, centerY) = latLonToTile(
                    latitude = descriptor.centerLatitude,
                    longitude = descriptor.centerLongitude,
                    zoom = z,
                )
                for (x in (centerX - 1)..(centerX + 1)) {
                    for (y in (centerY - 1)..(centerY + 1)) {
                        database.execSQL(
                            "INSERT OR REPLACE INTO tiles(zoom_level, tile_column, tile_row, tile_data) VALUES (?, ?, ?, ?)",
                            arrayOf(z, x, xyzToTms(z, y), tileBytes),
                        )
                    }
                }
            }
        } finally {
            database.close()
        }
    }

    private fun insertMetadata(
        database: SQLiteDatabase,
        name: String,
        value: String,
    ) {
        database.execSQL(
            "INSERT INTO metadata(name, value) VALUES (?, ?)",
            arrayOf(name, value),
        )
    }

    private fun buildCatalogJson(descriptors: List<DemoMissionDescriptor>): String {
        val entries = descriptors.joinToString(",\n") { descriptor ->
            """
            {
              "id": "${descriptor.id}",
              "name": "${descriptor.name}",
              "summary": "${descriptor.summary}",
              "downloadUrl": "${File(rootDirectory, "${descriptor.id}.jnavpack").toURI()}",
              "estimatedSizeBytes": ${descriptor.estimatedSizeBytes},
              "bounds": [${descriptor.westLongitude}, ${descriptor.southLatitude}, ${descriptor.eastLongitude}, ${descriptor.northLatitude}],
              "center": [${descriptor.centerLongitude}, ${descriptor.centerLatitude}],
              "minZoom": 9,
              "maxZoom": 16,
              "publisher": "${descriptor.publisher}",
              "trustHint": "${if (descriptor.signature == null) "UNVERIFIED" else "VERIFIED"}",
              "layers": {
                "topoVector": true,
                "hillshadeRaster": true,
                "imageryRaster": true
              }
            }
            """.trimIndent()
        }

        return """
            {
              "generatedAt": "2026-03-11T09:00:00Z",
              "packs": [
                $entries
              ]
            }
        """.trimIndent()
    }

    private fun buildManifestJson(descriptor: DemoMissionDescriptor): String {
        val checksum = "demo-${descriptor.id}"
        val signatureValue = descriptor.signature?.let { "\"$it\"" } ?: "null"
        return """
            {
              "id": "${descriptor.id}",
              "name": "${descriptor.name}",
              "version": "${descriptor.version}",
              "packageFormat": 1,
              "bounds": [${descriptor.westLongitude}, ${descriptor.southLatitude}, ${descriptor.eastLongitude}, ${descriptor.northLatitude}],
              "center": [${descriptor.centerLongitude}, ${descriptor.centerLatitude}],
              "minZoom": 9,
              "maxZoom": 16,
              "layers": {
                "topoVector": true,
                "hillshadeRaster": true,
                "imageryRaster": true
              },
              "tiles": {
                "vector": "tiles/vector.mbtiles",
                "hillshade": "tiles/hillshade.mbtiles",
                "imagery": "tiles/imagery.mbtiles"
              },
              "stylePath": "style/style.json",
              "thumbnailPath": "preview/thumbnail.png",
              "publisher": "${descriptor.publisher}",
              "signature": $signatureValue,
              "checksumSha256": "$checksum"
            }
        """.trimIndent()
    }

    private fun buildStyleJson(descriptor: DemoMissionDescriptor): String {
        return """
            {
              "version": 8,
              "name": "${descriptor.name}",
              "sources": {
                "imagery": {
                  "type": "raster",
                  "tiles": ["__IMAGERY_TILES__"],
                  "tileSize": 256
                },
                "hillshade": {
                  "type": "raster",
                  "tiles": ["__HILLSHADE_TILES__"],
                  "tileSize": 256
                },
                "topo": {
                  "type": "vector",
                  "tiles": ["__VECTOR_TILES__"],
                  "minzoom": 9,
                  "maxzoom": 16
                }
              },
              "layers": [
                {
                  "id": "background",
                  "type": "background",
                  "paint": {
                    "background-color": "${descriptor.backgroundColorHex}"
                  }
                },
                {
                  "id": "imagery-layer",
                  "type": "raster",
                  "source": "imagery",
                  "paint": {
                    "raster-opacity": 0.9
                  }
                },
                {
                  "id": "hillshade-layer",
                  "type": "raster",
                  "source": "hillshade",
                  "paint": {
                    "raster-opacity": 0.36
                  }
                },
                {
                  "id": "topo-fill",
                  "type": "fill",
                  "source": "topo",
                  "source-layer": "mission",
                  "paint": {
                    "fill-color": "${descriptor.topoFillColorHex}",
                    "fill-opacity": 0.28
                  }
                },
                {
                  "id": "topo-line",
                  "type": "line",
                  "source": "topo",
                  "source-layer": "mission",
                  "paint": {
                    "line-color": "${descriptor.topoLineColorHex}",
                    "line-width": 2.1
                  }
                },
                {
                  "id": "topo-point",
                  "type": "circle",
                  "source": "topo",
                  "source-layer": "mission",
                  "paint": {
                    "circle-radius": 4.4,
                    "circle-color": "${descriptor.pointColorHex}",
                    "circle-stroke-width": 1.2,
                    "circle-stroke-color": "#102312"
                  }
                }
              ]
            }
        """.trimIndent()
    }

    private fun buildPreviewPng(descriptor: DemoMissionDescriptor): ByteArray {
        val bitmap = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor(descriptor.backgroundColorHex))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(descriptor.topoFillColorHex)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(96f, 96f, 60f, paint)
        paint.color = Color.parseColor(descriptor.pointColorHex)
        canvas.drawCircle(96f, 78f, 18f, paint)

        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            output.toByteArray()
        }
    }

    private fun buildHillshadePng(descriptor: DemoMissionDescriptor): ByteArray {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor(descriptor.backgroundColorHex))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (index in 0 until 8) {
            val alpha = 18 + (index * 12)
            paint.color = Color.argb(alpha, 30, 30, 30)
            canvas.drawLine(
                0f,
                index * 34f,
                256f,
                (index * 34f) + 80f,
                paint,
            )
        }

        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            output.toByteArray()
        }
    }

    private fun buildImageryJpeg(descriptor: DemoMissionDescriptor): ByteArray {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor(descriptor.imageryBaseColorHex))
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor(descriptor.imageryAccentColorHex)
        for (index in 0 until 6) {
            val inset = 12f + (index * 18f)
            canvas.drawRect(inset, inset, 256f - inset, 256f - inset, paint)
            paint.color = Color.argb(
                200 - (index * 18),
                (descriptor.imageryAccentColor shr 16) and 0xFF,
                (descriptor.imageryAccentColor shr 8) and 0xFF,
                descriptor.imageryAccentColor and 0xFF,
            )
        }

        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 84, output)
            output.toByteArray()
        }
    }

    private fun zipDirectory(
        sourceDirectory: File,
        archiveFile: File,
    ) {
        ZipOutputStream(FileOutputStream(archiveFile)).use { zip ->
            sourceDirectory.walkTopDown()
                .filter(File::isFile)
                .forEach { file ->
                    val relativePath = file.relativeTo(sourceDirectory).invariantSeparatorsPath
                    zip.putNextEntry(ZipEntry(relativePath))
                    file.inputStream().use { input -> input.copyTo(zip) }
                    zip.closeEntry()
                }
        }
    }
}

private data class DemoMissionDescriptor(
    val id: String,
    val name: String,
    val summary: String,
    val version: String,
    val publisher: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val northLatitude: Double,
    val eastLongitude: Double,
    val southLatitude: Double,
    val westLongitude: Double,
    val estimatedSizeBytes: Long,
    val signature: String?,
    val backgroundColorHex: String,
    val topoFillColorHex: String,
    val topoLineColorHex: String,
    val pointColorHex: String,
    val imageryBaseColorHex: String,
    val imageryAccentColorHex: String,
) {
    val imageryAccentColor: Int
        get() = Color.parseColor(imageryAccentColorHex)
}

private fun demoDescriptors(): List<DemoMissionDescriptor> {
    return listOf(
        DemoMissionDescriptor(
            id = "gunung-gede-mission-a",
            name = "Gunung Gede Mission A",
            summary = "Topo, hillshade, and imagery bundle for the southern approach.",
            version = "2026.03.11",
            publisher = "JungleNav Labs",
            centerLatitude = -6.77,
            centerLongitude = 106.91,
            northLatitude = -6.71,
            eastLongitude = 107.01,
            southLatitude = -6.84,
            westLongitude = 106.98,
            estimatedSizeBytes = 4_800_000L,
            signature = null,
            backgroundColorHex = "#d8e3cf",
            topoFillColorHex = "#6ea971",
            topoLineColorHex = "#183f27",
            pointColorHex = "#ffd166",
            imageryBaseColorHex = "#687f45",
            imageryAccentColorHex = "#91a85e",
        ),
        DemoMissionDescriptor(
            id = "bogor-trails",
            name = "Bogor Trails",
            summary = "Mission-area bundle for humid ridge paths and trail recovery drills.",
            version = "2026.03.11",
            publisher = "Relief Ops Cartography",
            centerLatitude = -6.61,
            centerLongitude = 106.80,
            northLatitude = -6.54,
            eastLongitude = 106.88,
            southLatitude = -6.68,
            westLongitude = 106.72,
            estimatedSizeBytes = 4_600_000L,
            signature = "demo-signed-bogor",
            backgroundColorHex = "#d4e7dc",
            topoFillColorHex = "#5d9275",
            topoLineColorHex = "#123828",
            pointColorHex = "#ffcc80",
            imageryBaseColorHex = "#4e6f49",
            imageryAccentColorHex = "#7cb36b",
        ),
        DemoMissionDescriptor(
            id = "bandung-highlands",
            name = "Bandung Highlands",
            summary = "Higher-elevation mission bundle tuned for ridges, valleys, and recovery corridors.",
            version = "2026.03.11",
            publisher = "JungleNav Labs",
            centerLatitude = -6.91,
            centerLongitude = 107.61,
            northLatitude = -6.82,
            eastLongitude = 107.73,
            southLatitude = -7.00,
            westLongitude = 107.49,
            estimatedSizeBytes = 4_900_000L,
            signature = null,
            backgroundColorHex = "#dbe0c9",
            topoFillColorHex = "#7ba362",
            topoLineColorHex = "#233d1d",
            pointColorHex = "#ffd28a",
            imageryBaseColorHex = "#6e7b44",
            imageryAccentColorHex = "#a0b85f",
        ),
    )
}

private fun latLonToTile(
    latitude: Double,
    longitude: Double,
    zoom: Int,
): Pair<Int, Int> {
    val latRad = Math.toRadians(latitude)
    val n = 1 shl zoom
    val x = ((longitude + 180.0) / 360.0 * n).toInt()
    val y =
        (
            (1.0 - kotlin.math.asinh(kotlin.math.tan(latRad)) / Math.PI) / 2.0 * n
            ).toInt()
    return x to y
}

private fun xyzToTms(z: Int, y: Int): Int = ((1 shl z) - 1) - y

private object DemoVectorTileEncoder {
    fun buildMissionTile(): ByteArray {
        val layerBytes = message {
            writeStringField(1, "mission")
            writeMessageField(2, polygonFeature(1))
            writeMessageField(2, lineFeature(2))
            writeMessageField(2, pointFeature(3))
            writeVarintField(5, 4096)
            writeVarintField(15, 2)
        }

        return message {
            writeMessageField(3, layerBytes)
        }
    }

    private fun polygonFeature(id: Int): ByteArray {
        val geometry = intArrayOf(
            command(1, 1),
            zigZag(512),
            zigZag(512),
            command(2, 3),
            zigZag(3072),
            zigZag(0),
            zigZag(0),
            zigZag(3072),
            zigZag(-3072),
            zigZag(0),
            command(7, 1),
        )
        return feature(id = id, type = 3, geometry = geometry)
    }

    private fun lineFeature(id: Int): ByteArray {
        val geometry = intArrayOf(
            command(1, 1),
            zigZag(700),
            zigZag(3000),
            command(2, 2),
            zigZag(1300),
            zigZag(-2000),
            zigZag(1400),
            zigZag(1800),
        )
        return feature(id = id, type = 2, geometry = geometry)
    }

    private fun pointFeature(id: Int): ByteArray {
        val geometry = intArrayOf(
            command(1, 1),
            zigZag(2048),
            zigZag(2048),
        )
        return feature(id = id, type = 1, geometry = geometry)
    }

    private fun feature(
        id: Int,
        type: Int,
        geometry: IntArray,
    ): ByteArray {
        return message {
            writeVarintField(1, id)
            writeVarintField(3, type)
            writePackedField(4, geometry)
        }
    }

    private fun command(id: Int, count: Int): Int = (count shl 3) or id

    private fun zigZag(value: Int): Int = (value shl 1) xor (value shr 31)

    private inline fun message(block: ProtoWriter.() -> Unit): ByteArray {
        return ProtoWriter().apply(block).toByteArray()
    }
}

private class ProtoWriter {
    private val output = ByteArrayOutputStream()

    fun writeVarintField(fieldNumber: Int, value: Int) {
        writeTag(fieldNumber, 0)
        writeVarint(value.toLong())
    }

    fun writeStringField(fieldNumber: Int, value: String) {
        writeTag(fieldNumber, 2)
        val bytes = value.toByteArray()
        writeVarint(bytes.size.toLong())
        output.write(bytes)
    }

    fun writeMessageField(fieldNumber: Int, bytes: ByteArray) {
        writeTag(fieldNumber, 2)
        writeVarint(bytes.size.toLong())
        output.write(bytes)
    }

    fun writePackedField(fieldNumber: Int, values: IntArray) {
        val packed = ByteArrayOutputStream()
        values.forEach { value ->
            writeVarintTo(packed, value.toLong())
        }
        val bytes = packed.toByteArray()
        writeTag(fieldNumber, 2)
        writeVarint(bytes.size.toLong())
        output.write(bytes)
    }

    fun toByteArray(): ByteArray = output.toByteArray()

    private fun writeTag(fieldNumber: Int, wireType: Int) {
        writeVarint(((fieldNumber shl 3) or wireType).toLong())
    }

    private fun writeVarint(value: Long) {
        writeVarintTo(output, value)
    }
}

private fun writeVarintTo(
    output: ByteArrayOutputStream,
    value: Long,
) {
    var remaining = value
    while (true) {
        if (remaining and 0x7FL.inv() == 0L) {
            output.write(remaining.toInt())
            return
        }
        output.write((remaining.toInt() and 0x7F) or 0x80)
        remaining = remaining ushr 7
    }
}
