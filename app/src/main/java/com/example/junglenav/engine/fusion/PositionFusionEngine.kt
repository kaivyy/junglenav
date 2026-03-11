package com.example.junglenav.engine.fusion

import com.example.junglenav.engine.positioning.LocationSample
import com.example.junglenav.engine.positioning.NavigationSource
import com.example.junglenav.engine.positioning.PositionEstimate
import com.example.junglenav.engine.deadreckoning.DeadReckoningEngine
import com.example.junglenav.engine.matching.MatchedPosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PositionFusionEngine(
    private val fusionSolver: FusionSolver = WeightedFusionSolver(),
    private val deadReckoningEngine: DeadReckoningEngine = DeadReckoningEngine(),
) {
    private val _estimate = MutableStateFlow(PositionEstimate.noFix())
    val estimate: StateFlow<PositionEstimate> = _estimate.asStateFlow()
    private var latestLocationSample: LocationSample? = null
    private var latestHeadingEstimate: HeadingEstimate? = null
    private var matchedPosition: MatchedPosition? = null
    private var terrainPenalty: Int = 0

    fun onLocationSample(sample: LocationSample) {
        latestLocationSample = sample
        recompute(nowEpochMs = sample.timestampEpochMs)
    }

    fun onHeadingEstimate(estimate: HeadingEstimate) {
        latestHeadingEstimate = estimate
        recompute(nowEpochMs = _estimate.value.timestampEpochMs)
    }

    fun onMatchedPosition(matchedPosition: MatchedPosition?) {
        this.matchedPosition = matchedPosition
        recompute(nowEpochMs = _estimate.value.timestampEpochMs)
    }

    fun onTerrainPenalty(terrainPenalty: Int) {
        this.terrainPenalty = terrainPenalty
        recompute(nowEpochMs = _estimate.value.timestampEpochMs)
    }

    fun onTick(nowEpochMs: Long) {
        recompute(nowEpochMs = nowEpochMs)
    }

    private fun recompute(nowEpochMs: Long) {
        val locationSample = latestLocationSample
        val locationAgeMs = locationSample?.let { (nowEpochMs - it.timestampEpochMs).coerceAtLeast(0L) }
        val deadReckoningEstimate = if (locationSample != null && locationAgeMs != null && locationAgeMs > 15_000L) {
            deadReckoningEngine.projectWithoutGnss(
                elapsedMs = locationAgeMs,
                stepCountDelta = (locationAgeMs / 1_000L).toInt(),
                headingDegrees = latestHeadingEstimate?.headingDegrees ?: locationSample.bearingDegrees,
                anchorEstimate = _estimate.value.takeIf { it.mode != com.example.junglenav.core.model.PositionMode.NO_FIX }
                    ?: PositionEstimate.fromSample(locationSample).copy(
                        source = NavigationSource.FUSED,
                    ),
            )
        } else {
            null
        }

        _estimate.value = fusionSolver.solve(
            HybridNavigationSnapshot(
                latestLocationSample = locationSample,
                locationAgeMs = locationAgeMs,
                latestHeadingEstimate = latestHeadingEstimate,
                deadReckoningEstimate = deadReckoningEstimate,
                matchedPosition = matchedPosition,
                terrainPenalty = terrainPenalty,
            ),
        )
    }
}
