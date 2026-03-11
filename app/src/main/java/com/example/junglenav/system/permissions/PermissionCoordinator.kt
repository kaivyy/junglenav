package com.example.junglenav.system.permissions

import android.Manifest

class PermissionCoordinator {
    fun needsBackgroundLocation(isRecording: Boolean): Boolean = isRecording

    fun requiredRuntimePermissions(
        isRecording: Boolean,
        needsNotificationPermission: Boolean,
    ): List<String> {
        return buildList {
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (needsBackgroundLocation(isRecording)) {
                add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            if (needsNotificationPermission) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
