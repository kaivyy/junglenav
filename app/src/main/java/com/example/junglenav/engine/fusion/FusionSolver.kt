package com.example.junglenav.engine.fusion

import com.example.junglenav.engine.positioning.PositionEstimate

interface FusionSolver {
    fun solve(snapshot: HybridNavigationSnapshot): PositionEstimate
}
