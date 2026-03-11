package com.example.junglenav.system.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.junglenav.R

object TrackingNotificationFactory {
    const val CHANNEL_ID = "junglenav_tracking"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "JungleNav Tracking",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Foreground tracking status for JungleNav field sessions."
        }
        manager.createNotificationChannel(channel)
    }

    fun create(
        context: Context,
        title: String,
        summary: String,
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(summary)
            .setOngoing(true)
            .build()
    }
}
