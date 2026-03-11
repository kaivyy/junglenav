package com.example.junglenav.engine.positioning

import kotlinx.coroutines.flow.Flow

interface GnssStatusSource {
    fun observeGnssStatusSamples(): Flow<GnssStatusSample>
}
