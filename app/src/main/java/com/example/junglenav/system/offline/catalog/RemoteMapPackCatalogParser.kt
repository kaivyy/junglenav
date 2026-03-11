package com.example.junglenav.system.offline.catalog

import com.example.junglenav.core.model.MapPackLayerSet
import com.example.junglenav.core.model.MapPackTrust
import com.example.junglenav.core.model.RemoteMapPackCatalogItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class RemoteMapPackCatalogParser {
    fun parse(json: String): List<RemoteMapPackCatalogItem> {
        val root = Json.parseToJsonElement(json).jsonObject
        return root["packs"]
            ?.jsonArray
            ?.map(::parseEntry)
            ?.sortedBy(RemoteMapPackCatalogItem::name)
            .orEmpty()
    }

    private fun parseEntry(element: JsonElement): RemoteMapPackCatalogItem {
        val entry = element.jsonObject
        val bounds = entry.requireArray("bounds", expectedSize = 4)
        val center = entry.requireArray("center", expectedSize = 2)
        val layers = entry["layers"]?.jsonObject ?: JsonObject(emptyMap())

        return RemoteMapPackCatalogItem(
            id = entry.requireString("id"),
            name = entry.requireString("name"),
            summary = entry.requireString("summary"),
            downloadUrl = entry.requireString("downloadUrl"),
            estimatedSizeBytes = entry.requireLong("estimatedSizeBytes"),
            westLongitude = bounds[0].jsonPrimitive.requireDouble("bounds[0]"),
            southLatitude = bounds[1].jsonPrimitive.requireDouble("bounds[1]"),
            eastLongitude = bounds[2].jsonPrimitive.requireDouble("bounds[2]"),
            northLatitude = bounds[3].jsonPrimitive.requireDouble("bounds[3]"),
            centerLongitude = center[0].jsonPrimitive.requireDouble("center[0]"),
            centerLatitude = center[1].jsonPrimitive.requireDouble("center[1]"),
            minZoom = entry.requireDouble("minZoom"),
            maxZoom = entry.requireDouble("maxZoom"),
            publisher = entry.optionalString("publisher"),
            trustHint = entry.optionalString("trustHint")?.toMapPackTrust() ?: MapPackTrust.UNVERIFIED,
            layers = MapPackLayerSet(
                topoVector = layers.booleanValue("topoVector"),
                hillshadeRaster = layers.booleanValue("hillshadeRaster"),
                imageryRaster = layers.booleanValue("imageryRaster"),
            ),
        )
    }
}

private fun JsonObject.requireArray(key: String, expectedSize: Int): JsonArray {
    val array = this[key]?.jsonArray ?: error("Missing $key")
    require(array.size == expectedSize) { "Invalid $key length" }
    return array
}

private fun JsonObject.requireString(key: String): String {
    return this[key]?.jsonPrimitive?.content?.takeIf(String::isNotBlank) ?: error("Missing $key")
}

private fun JsonObject.optionalString(key: String): String? {
    val element = this[key] ?: return null
    if (element.toString() == "null") return null
    return (element as? JsonPrimitive)?.content?.takeIf(String::isNotBlank)
}

private fun JsonObject.requireLong(key: String): Long {
    return this[key]?.jsonPrimitive?.longOrNull ?: error("Missing $key")
}

private fun JsonObject.requireDouble(key: String): Double {
    return this[key]?.jsonPrimitive?.doubleOrNull ?: error("Missing $key")
}

private fun JsonPrimitive.requireDouble(key: String): Double {
    return doubleOrNull ?: error("Missing $key")
}

private fun JsonObject.booleanValue(key: String): Boolean {
    return this[key]?.jsonPrimitive?.booleanOrNull ?: false
}

private fun String.toMapPackTrust(): MapPackTrust {
    return runCatching { MapPackTrust.valueOf(this) }
        .getOrDefault(MapPackTrust.UNVERIFIED)
}
