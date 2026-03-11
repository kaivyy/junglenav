package com.example.junglenav.system.offline.jnavpack

import com.example.junglenav.core.model.MapPackSource
import com.example.junglenav.core.model.MapPackTrust
import com.example.junglenav.core.model.MapPackage
import java.io.File

class JnavPackInstaller(
    private val extractor: JnavPackArchiveExtractor,
    private val parser: JnavPackManifestParser,
    private val validator: JnavPackValidator,
    private val trustResolver: JnavPackTrustResolver,
    private val installRoot: File,
) {
    fun install(
        archiveFile: File,
        source: MapPackSource,
    ): JnavPackInstallResult {
        installRoot.mkdirs()
        val stagingDirectory = File(installRoot, "${archiveFile.nameWithoutExtension}-staging")
        val extractedDirectory = extractor.extract(
            archiveFile = archiveFile,
            targetDirectory = stagingDirectory,
        )

        val manifestFile = File(extractedDirectory, "manifest.json")
        require(manifestFile.exists()) { "Missing manifest.json" }

        val manifest = parser.parse(manifestFile.readText())
        val validation = validator.validate(manifest)
        require(validation.isValid) {
            "Invalid jnavpack: ${validation.errors.joinToString()}"
        }

        val installDirectory = File(installRoot, manifest.id)
        if (installDirectory.exists()) {
            installDirectory.deleteRecursively()
        }
        extractedDirectory.copyRecursively(installDirectory, overwrite = true)
        extractedDirectory.deleteRecursively()

        val trust = trustResolver.resolve(manifest)
        val styleFile = File(installDirectory, manifest.stylePath)
        val thumbnailFile =
            listOf(
                File(installDirectory, "preview/thumbnail.webp"),
                File(installDirectory, "preview/thumbnail.png"),
            ).firstOrNull(File::exists)

        return JnavPackInstallResult(
            mapPackage = MapPackage(
                id = manifest.id,
                name = manifest.name,
                version = manifest.version,
                sizeBytes = installDirectory.directorySizeBytes(),
                isActive = false,
                filePath = styleFile.toURI().toString(),
                checksum = manifest.checksumSha256 ?: archiveFile.nameWithoutExtension,
                isDownloaded = true,
                centerLatitude = manifest.center.latitude,
                centerLongitude = manifest.center.longitude,
                installRootPath = installDirectory.absolutePath,
                manifestPath = File(installDirectory, "manifest.json").absolutePath,
                source = source,
                trust = trust,
                publisher = manifest.publisher,
                requiresActivationConfirmation = trust != MapPackTrust.VERIFIED,
                layers = manifest.layers,
                thumbnailPath = thumbnailFile?.absolutePath,
            ),
            installDirectory = installDirectory,
        )
    }
}

private fun File.directorySizeBytes(): Long {
    return walkTopDown()
        .filter(File::isFile)
        .sumOf(File::length)
}
