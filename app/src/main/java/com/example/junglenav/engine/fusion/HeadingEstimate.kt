package com.example.junglenav.engine.fusion

data class HeadingEstimate(
    val headingDegrees: Float,
    val confidence: Int,
    val sourceLabel: String,
)
