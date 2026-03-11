package com.example.junglenav.system.replay

import com.example.junglenav.engine.sensors.BarometerSample
import com.example.junglenav.engine.sensors.BarometerSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReplayBarometerSource(
    private val samples: List<BarometerSample>,
) : BarometerSource {
    override fun observeBarometerSamples(): Flow<BarometerSample> = flow {
        samples
            .sortedBy { it.timestampEpochMs }
            .forEach { emit(it) }
    }
}
