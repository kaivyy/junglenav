package com.example.junglenav.feature.diagnostics

import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsViewModelTest {
    @Test
    fun syncStateShowsHardwareReadinessInsteadOfPendingPlaceholders() {
        val viewModel = DiagnosticsViewModel()

        viewModel.syncSensorReadiness(
            gnssReady = true,
            inertialReady = true,
            barometerReady = false,
        )

        assertTrue(viewModel.uiState.value.sensorAvailability.contains("GNSS adapter ready (live)"))
        assertTrue(viewModel.uiState.value.sensorAvailability.contains("IMU adapter ready (live)"))
        assertTrue(viewModel.uiState.value.sensorAvailability.contains("Barometer adapter unavailable"))
    }
}
