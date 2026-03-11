package com.example.junglenav.system.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.junglenav.engine.sensors.BarometerSample
import com.example.junglenav.engine.sensors.BarometerSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidBarometerSource(
    private val sensorManager: SensorManager,
    private val samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_NORMAL,
) : BarometerSource {
    override fun observeBarometerSamples(): Flow<BarometerSample> = callbackFlow {
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        if (pressureSensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val pressure = event.values.firstOrNull() ?: return
                trySend(
                    BarometerSample(
                        pressureHpa = pressure,
                        altitudeMeters = SensorManager.getAltitude(
                            SensorManager.PRESSURE_STANDARD_ATMOSPHERE,
                            pressure,
                        ),
                        timestampEpochMs = System.currentTimeMillis(),
                    ),
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, pressureSensor, samplingPeriodUs)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
