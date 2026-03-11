package com.example.junglenav.system.offline

data class OfflineRegionDownloadProgress(
    val completedResources: Long,
    val requiredResources: Long,
    val completedBytes: Long,
) {
    val progressPercent: Int
        get() = if (requiredResources <= 0L) {
            0
        } else {
            ((completedResources * 100L) / requiredResources).toInt().coerceIn(0, 100)
        }
}
