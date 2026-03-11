package com.example.junglenav.data.settings

import com.example.junglenav.core.model.OperationMode

class OperationModePreferences(
    private val storage: MutableMap<String, String>,
) {
    suspend fun readMode(): OperationMode {
        return storage[MODE_KEY]
            ?.let(::operationModeFromStorageValue)
            ?: OperationMode.PATROL
    }

    suspend fun writeMode(operationMode: OperationMode) {
        storage[MODE_KEY] = operationMode.name
    }

    companion object {
        private const val MODE_KEY = "operation_mode"
    }
}

private fun operationModeFromStorageValue(value: String): OperationMode {
    return OperationMode.entries.firstOrNull { it.name == value } ?: OperationMode.PATROL
}
