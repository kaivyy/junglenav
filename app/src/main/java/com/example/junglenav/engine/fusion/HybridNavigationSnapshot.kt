package com.example.junglenav.engine.fusion

import com.example.junglenav.engine.matching.MatchedPosition
import com.example.junglenav.engine.positioning.LocationSample
import com.example.junglenav.engine.positioning.PositionEstimate

data class HybridNavigationSnapshot(
    val latestLocationSample: LocationSample? = null,
    val locationAgeMs: Long? = null,
    val latestHeadingEstimate: HeadingEstimate? = null,
    val deadReckoningEstimate: PositionEstimate? = null,
    val matchedPosition: MatchedPosition? = null,
    val terrainPenalty: Int = 0,
)
