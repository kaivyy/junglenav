package com.example.junglenav.engine.sensors

import kotlinx.coroutines.flow.Flow

interface BarometerSource {
    fun observeBarometerSamples(): Flow<BarometerSample>
}
