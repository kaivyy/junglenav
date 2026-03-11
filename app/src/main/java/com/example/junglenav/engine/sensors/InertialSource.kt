package com.example.junglenav.engine.sensors

import kotlinx.coroutines.flow.Flow

interface InertialSource {
    fun observeInertialSamples(): Flow<InertialSample>
}
