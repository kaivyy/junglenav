package com.example.junglenav.engine.fusion

import com.example.junglenav.engine.positioning.PositionEstimate

class KalmanFusionSolver(
    private val fallbackSolver: WeightedFusionSolver = WeightedFusionSolver(),
) : FusionSolver {
    override fun solve(snapshot: HybridNavigationSnapshot): PositionEstimate {
        return fallbackSolver.solve(snapshot)
    }

    fun solve(scenario: FusionReplayScenario): PositionEstimate {
        val latestSample = scenario.samples.lastOrNull()
        return fallbackSolver.solve(
            HybridNavigationSnapshot(
                latestLocationSample = latestSample,
                locationAgeMs = 0L,
            ),
        )
    }
}
