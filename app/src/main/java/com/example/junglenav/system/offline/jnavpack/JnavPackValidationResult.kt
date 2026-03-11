package com.example.junglenav.system.offline.jnavpack

data class JnavPackValidationResult(
    val errors: List<String>,
) {
    val isValid: Boolean
        get() = errors.isEmpty()
}
