package com.example.junglenav.system.location

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.example.junglenav.engine.positioning.LocationSample
import com.example.junglenav.engine.positioning.LocationSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidLocationSource(
    private val locationManager: LocationManager,
    private val provider: String = LocationManager.GPS_PROVIDER,
    private val minTimeMs: Long = 1_000L,
    private val minDistanceMeters: Float = 0f,
) : LocationSource {
    @SuppressLint("MissingPermission")
    override fun observeLocationSamples(): Flow<LocationSample> = callbackFlow {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location.toSample())
            }

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) = Unit

            @Deprecated("Deprecated in API 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }

        try {
            locationManager.requestLocationUpdates(
                provider,
                minTimeMs,
                minDistanceMeters,
                listener,
                Looper.getMainLooper(),
            )
        } catch (_: SecurityException) {
            close()
        } catch (_: IllegalArgumentException) {
            close()
        }

        awaitClose {
            locationManager.removeUpdates(listener)
        }
    }

    private fun Location.toSample(): LocationSample {
        return LocationSample(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracy,
            speedMps = speed,
            bearingDegrees = bearing,
            timestampEpochMs = time,
        )
    }
}
