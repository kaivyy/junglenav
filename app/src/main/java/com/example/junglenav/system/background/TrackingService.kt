package com.example.junglenav.system.background

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

class TrackingService : Service() {
    override fun onCreate() {
        super.onCreate()
        TrackingNotificationFactory.ensureChannel(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        return when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                START_NOT_STICKY
            }

            else -> {
                val summary = intent?.getStringExtra(EXTRA_STATUS_SUMMARY) ?: "Awaiting field samples"
                val notification = TrackingNotificationFactory.create(
                    context = this,
                    title = "JungleNav tracking active",
                    summary = summary,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        TrackingNotificationFactory.NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
                    )
                } else {
                    startForeground(TrackingNotificationFactory.NOTIFICATION_ID, notification)
                }
                START_STICKY
            }
        }
    }

    companion object {
        const val ACTION_START = "com.example.junglenav.action.START_TRACKING"
        const val ACTION_STOP = "com.example.junglenav.action.STOP_TRACKING"
        const val EXTRA_STATUS_SUMMARY = "extra_status_summary"
    }
}
