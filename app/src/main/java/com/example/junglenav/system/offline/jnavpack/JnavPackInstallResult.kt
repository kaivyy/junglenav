package com.example.junglenav.system.offline.jnavpack

import com.example.junglenav.core.model.MapPackage
import java.io.File

data class JnavPackInstallResult(
    val mapPackage: MapPackage,
    val installDirectory: File,
)
