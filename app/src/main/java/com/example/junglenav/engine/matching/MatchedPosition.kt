package com.example.junglenav.engine.matching

data class PathCandidate(
    val pathId: String,
    val latitude: Double,
    val longitude: Double,
)

data class MatchedPosition(
    val pathId: String,
    val latitude: Double,
    val longitude: Double,
    val penaltyScore: Int,
)
