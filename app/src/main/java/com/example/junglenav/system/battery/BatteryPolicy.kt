package com.example.junglenav.system.battery

import com.example.junglenav.core.model.OperationMode

enum class DiagnosticsVerbosity {
    MINIMAL,
    STANDARD,
    VERBOSE,
}

data class BatteryProfile(
    val locationIntervalMs: Long,
    val sensorIntervalMs: Long,
    val diagnosticsVerbosity: DiagnosticsVerbosity,
)

object BatteryPolicy {
    fun forMode(operationMode: OperationMode): BatteryProfile {
        return when (operationMode) {
            OperationMode.PATROL -> BatteryProfile(
                locationIntervalMs = 3_000L,
                sensorIntervalMs = 2_000L,
                diagnosticsVerbosity = DiagnosticsVerbosity.STANDARD,
            )

            OperationMode.SURVEY -> BatteryProfile(
                locationIntervalMs = 1_500L,
                sensorIntervalMs = 1_000L,
                diagnosticsVerbosity = DiagnosticsVerbosity.VERBOSE,
            )

            OperationMode.BATTERY_SAVER -> BatteryProfile(
                locationIntervalMs = 10_000L,
                sensorIntervalMs = 6_000L,
                diagnosticsVerbosity = DiagnosticsVerbosity.MINIMAL,
            )

            OperationMode.EMERGENCY -> BatteryProfile(
                locationIntervalMs = 1_000L,
                sensorIntervalMs = 500L,
                diagnosticsVerbosity = DiagnosticsVerbosity.VERBOSE,
            )
        }
    }
}
