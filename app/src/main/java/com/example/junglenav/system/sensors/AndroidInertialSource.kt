package com.example.junglenav.system.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.junglenav.engine.sensors.InertialSample
import com.example.junglenav.engine.sensors.InertialSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidInertialSource(
    private val sensorManager: SensorManager,
    private val samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_GAME,
) : InertialSource {
    override fun observeInertialSamples(): Flow<InertialSample> = callbackFlow {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accelerometer == null || gyroscope == null) {
            close()
            return@callbackFlow
        }

        var lastAccel = floatArrayOf(0f, 0f, 0f)
        var lastGyro = floatArrayOf(0f, 0f, 0f)
        var lastMag = floatArrayOf(0f, 0f, 0f)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> lastAccel = event.values.copyOf()
                    Sensor.TYPE_GYROSCOPE -> lastGyro = event.values.copyOf()
                    Sensor.TYPE_MAGNETIC_FIELD -> lastMag = event.values.copyOf()
                }

                trySend(
                    InertialSample(
                        accelerationXMps2 = lastAccel[0],
                        accelerationYMps2 = lastAccel[1],
                        accelerationZMps2 = lastAccel[2],
                        gyroXRadPerSec = lastGyro[0],
                        gyroYRadPerSec = lastGyro[1],
                        gyroZRadPerSec = lastGyro[2],
                        magneticHeadingDegrees = computeMagneticHeadingDegrees(lastAccel, lastMag),
                        timestampEpochMs = System.currentTimeMillis(),
                    ),
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, accelerometer, samplingPeriodUs)
        sensorManager.registerListener(listener, gyroscope, samplingPeriodUs)
        magnetometer?.let { sensorManager.registerListener(listener, it, samplingPeriodUs) }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    private fun computeMagneticHeadingDegrees(
        gravity: FloatArray,
        geomagnetic: FloatArray,
    ): Float? {
        if (geomagnetic.all { it == 0f }) {
            return null
        }

        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            inclinationMatrix,
            gravity,
            geomagnetic,
        )
        if (!success) {
            return null
        }

        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)
        val azimuthDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
        return if (azimuthDegrees < 0f) azimuthDegrees + 360f else azimuthDegrees
    }
}
