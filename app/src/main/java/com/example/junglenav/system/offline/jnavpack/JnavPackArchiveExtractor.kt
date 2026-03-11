package com.example.junglenav.system.offline.jnavpack

import java.io.File
import java.util.zip.ZipInputStream

class JnavPackArchiveExtractor {
    fun extract(
        archiveFile: File,
        targetDirectory: File,
    ): File {
        val rootDirectory = targetDirectory.canonicalFile
        if (targetDirectory.exists()) {
            targetDirectory.deleteRecursively()
        }
        targetDirectory.mkdirs()

        ZipInputStream(archiveFile.inputStream().buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val resolved = targetDirectory.resolve(entry.name).canonicalFile
                require(resolved.path.startsWith(rootDirectory.path)) {
                    "Invalid archive entry: ${entry.name}"
                }

                if (entry.isDirectory) {
                    resolved.mkdirs()
                } else {
                    resolved.parentFile?.mkdirs()
                    resolved.outputStream().buffered().use { output ->
                        zip.copyTo(output)
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        return targetDirectory
    }
}
