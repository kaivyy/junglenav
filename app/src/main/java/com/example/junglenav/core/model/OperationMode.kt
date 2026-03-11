package com.example.junglenav.core.model

enum class OperationMode(val locationIntervalMs: Long) {
    PATROL(locationIntervalMs = 3_000L),
    SURVEY(locationIntervalMs = 1_500L),
    BATTERY_SAVER(locationIntervalMs = 10_000L),
    EMERGENCY(locationIntervalMs = 1_000L),
}
