package com.example.junglenav.system.offline.jnavpack

import com.example.junglenav.core.model.MapPackLayerSet
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class JnavPackManifestParser(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun parse(source: String): JnavPackManifest {
        val root = json.parseToJsonElement(source).jsonObject
        val layers = root.optionalObject("layers")
        val tiles = root.optionalObject("tiles")

        return JnavPackManifest(
            id = root.requiredString("id"),
            name = root.requiredString("name"),
            version = root.requiredString("version"),
            packageFormat = root.requiredInt("packageFormat"),
            bounds = root.requiredBounds("bounds"),
            center = root.requiredCenter("center"),
            minZoom = root.requiredDouble("minZoom"),
            maxZoom = root.requiredDouble("maxZoom"),
            layers = MapPackLayerSet(
                topoVector = layers.optionalBoolean("topoVector", defaultValue = true),
                hillshadeRaster = layers.optionalBoolean("hillshadeRaster"),
                imageryRaster = layers.optionalBoolean("imageryRaster"),
            ),
            stylePath = root.requiredString("stylePath"),
            vectorTilesPath = tiles.optionalString("vector"),
            hillshadeTilesPath = tiles.optionalString("hillshade"),
            imageryTilesPath = tiles.optionalString("imagery"),
            publisher = root.optionalString("publisher"),
            signature = root.optionalString("signature"),
            checksumSha256 = root.optionalString("checksumSha256"),
        )
    }
}

private fun JsonObject.requiredString(key: String): String {
    return this[key]?.jsonPrimitive?.content ?: error("Missing required key: $key")
}

private fun JsonObject.optionalString(key: String): String? {
    val element = this[key] ?: return null
    if (element.toString() == "null") {
        return null
    }
    return element.jsonPrimitive.content
}

private fun JsonObject.requiredInt(key: String): Int {
    return this[key]?.jsonPrimitive?.content?.toIntOrNull() ?: error("Missing required int: $key")
}

private fun JsonObject.requiredDouble(key: String): Double {
    return this[key]?.jsonPrimitive?.content?.toDoubleOrNull() ?: error("Missing required double: $key")
}

private fun JsonObject.requiredBounds(key: String): JnavPackBounds {
    val values = this[key] as? JsonArray ?: error("Missing required bounds: $key")
    require(values.size == 4) { "Bounds must contain four coordinates" }
    return JnavPackBounds(
        westLongitude = values[0].requiredDouble(),
        southLatitude = values[1].requiredDouble(),
        eastLongitude = values[2].requiredDouble(),
        northLatitude = values[3].requiredDouble(),
    )
}

private fun JsonObject.requiredCenter(key: String): JnavPackCenter {
    val values = this[key] as? JsonArray ?: error("Missing required center: $key")
    require(values.size == 2) { "Center must contain two coordinates" }
    return JnavPackCenter(
        longitude = values[0].requiredDouble(),
        latitude = values[1].requiredDouble(),
    )
}

private fun JsonObject.optionalObject(key: String): JsonObject {
    return (this[key] as? JsonObject) ?: JsonObject(emptyMap())
}

private fun JsonObject.optionalBoolean(
    key: String,
    defaultValue: Boolean = false,
): Boolean {
    return this[key]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: defaultValue
}

private fun JsonElement.requiredDouble(): Double {
    return jsonPrimitive.content.toDouble()
}
