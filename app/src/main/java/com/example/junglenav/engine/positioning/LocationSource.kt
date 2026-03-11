package com.example.junglenav.engine.positioning

import kotlinx.coroutines.flow.Flow

interface LocationSource {
    fun observeLocationSamples(): Flow<LocationSample>
}
