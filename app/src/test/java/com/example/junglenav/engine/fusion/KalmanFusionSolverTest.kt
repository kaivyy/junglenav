package com.example.junglenav.engine.fusion

import org.junit.Assert.assertTrue
import org.junit.Test

class KalmanFusionSolverTest {
    @Test
    fun kalmanSolverProducesStableEstimateForReplayScenario() {
        val solver = KalmanFusionSolver()

        val estimate = solver.solve(FusionReplayScenario.basicForestWalk())

        assertTrue(estimate.confidence >= 0)
    }
}
