package com.example.junglenav.system.offline.jnavpack

class JnavPackValidator {
    fun validate(manifest: JnavPackManifest): JnavPackValidationResult {
        val errors = buildList {
            if (manifest.packageFormat != 1) {
                add("Unsupported packageFormat")
            }
            if (manifest.stylePath.isBlank()) {
                add("Missing stylePath")
            }
            if (manifest.vectorTilesPath.isNullOrBlank()) {
                add("Missing vectorTilesPath")
            }
            if (manifest.minZoom > manifest.maxZoom) {
                add("Invalid zoom range")
            }
            if (manifest.layers.hillshadeRaster && manifest.hillshadeTilesPath.isNullOrBlank()) {
                add("Missing hillshadeTilesPath")
            }
            if (manifest.layers.imageryRaster && manifest.imageryTilesPath.isNullOrBlank()) {
                add("Missing imageryTilesPath")
            }
        }
        return JnavPackValidationResult(errors = errors)
    }
}
