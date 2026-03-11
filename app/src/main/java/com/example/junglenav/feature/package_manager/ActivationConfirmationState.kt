package com.example.junglenav.feature.package_manager

data class ActivationConfirmationState(
    val packageId: String,
    val packageName: String,
    val publisher: String?,
    val checksum: String,
    val trustLabel: String,
)
