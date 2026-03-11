package com.example.junglenav.system.replay

import com.example.junglenav.engine.sensors.InertialSample
import com.example.junglenav.engine.sensors.InertialSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReplayInertialSource(
    private val samples: List<InertialSample>,
) : InertialSource {
    override fun observeInertialSamples(): Flow<InertialSample> = flow {
        samples
            .sortedBy { it.timestampEpochMs }
            .forEach { emit(it) }
    }
}
