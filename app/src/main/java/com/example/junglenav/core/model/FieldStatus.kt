package com.example.junglenav.core.model

data class FieldStatus(
    val mode: PositionMode,
    val operationMode: OperationMode,
    val confidenceLevel: ConfidenceLevel,
    val locationIntervalMs: Long,
) {
    companion object {
        fun forMode(operationMode: OperationMode): FieldStatus {
            return FieldStatus(
                mode = PositionMode.FUSED,
                operationMode = operationMode,
                confidenceLevel = ConfidenceLevel.MEDIUM,
                locationIntervalMs = operationMode.locationIntervalMs,
            )
        }
    }
}
