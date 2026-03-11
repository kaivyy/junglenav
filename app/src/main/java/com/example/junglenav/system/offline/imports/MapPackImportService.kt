package com.example.junglenav.system.offline.imports

import com.example.junglenav.core.model.MapPackage

fun interface MapPackImportService {
    suspend fun import(source: ImportedMapPackSource): MapPackage
}
