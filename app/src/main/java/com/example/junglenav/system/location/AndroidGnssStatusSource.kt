package com.example.junglenav.system.location

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import com.example.junglenav.engine.positioning.GnssStatusSample
import com.example.junglenav.engine.positioning.GnssStatusSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidGnssStatusSource(
    private val locationManager: LocationManager,
) : GnssStatusSource {
    @SuppressLint("MissingPermission")
    override fun observeGnssStatusSamples(): Flow<GnssStatusSample> = callbackFlow {
        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val satellitesUsed = (0 until status.satelliteCount).count { status.usedInFix(it) }
                val constellations = (0 until status.satelliteCount)
                    .filter { status.usedInFix(it) }
                    .map { status.getConstellationType(it).toConstellationName() }
                    .toSet()

                trySend(
                    GnssStatusSample(
                        satellitesUsed = satellitesUsed,
                        constellationNames = constellations,
                        timestampEpochMs = System.currentTimeMillis(),
                    ),
                )
            }
        }

        try {
            locationManager.registerGnssStatusCallback(callback, null)
        } catch (_: SecurityException) {
            close()
        }

        awaitClose {
            locationManager.unregisterGnssStatusCallback(callback)
        }
    }

    private fun Int.toConstellationName(): String {
        return when (this) {
            GnssStatus.CONSTELLATION_GPS -> "GPS"
            GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
            GnssStatus.CONSTELLATION_GALILEO -> "Galileo"
            GnssStatus.CONSTELLATION_BEIDOU -> "BeiDou"
            GnssStatus.CONSTELLATION_QZSS -> "QZSS"
            GnssStatus.CONSTELLATION_SBAS -> "SBAS"
            GnssStatus.CONSTELLATION_IRNSS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "NavIC" else "IRNSS"
            else -> "Unknown"
        }
    }
}
