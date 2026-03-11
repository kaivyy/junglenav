package com.example.junglenav.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.junglenav.core.model.OperationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    val operationMode: Flow<OperationMode> = dataStore.data.map { preferences ->
        preferences[OPERATION_MODE_KEY]
            ?.let { value -> OperationMode.entries.firstOrNull { it.name == value } }
            ?: OperationMode.PATROL
    }

    val units: Flow<String> = dataStore.data.map { preferences ->
        preferences[UNITS_KEY] ?: DEFAULT_UNITS
    }

    val activePackageId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ACTIVE_PACKAGE_ID_KEY]
    }

    val lowLightModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[LOW_LIGHT_MODE_KEY] ?: false
    }

    suspend fun setOperationMode(operationMode: OperationMode) {
        dataStore.edit { preferences ->
            preferences[OPERATION_MODE_KEY] = operationMode.name
        }
    }

    suspend fun setUnits(units: String) {
        dataStore.edit { preferences ->
            preferences[UNITS_KEY] = units
        }
    }

    suspend fun setActivePackageId(packageId: String?) {
        dataStore.edit { preferences ->
            if (packageId == null) {
                preferences.remove(ACTIVE_PACKAGE_ID_KEY)
            } else {
                preferences[ACTIVE_PACKAGE_ID_KEY] = packageId
            }
        }
    }

    suspend fun setLowLightModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[LOW_LIGHT_MODE_KEY] = enabled
        }
    }

    companion object {
        private const val DEFAULT_UNITS = "metric"
        private val OPERATION_MODE_KEY = stringPreferencesKey("operation_mode")
        private val UNITS_KEY = stringPreferencesKey("units")
        private val ACTIVE_PACKAGE_ID_KEY = stringPreferencesKey("active_package_id")
        private val LOW_LIGHT_MODE_KEY = booleanPreferencesKey("low_light_mode_enabled")
    }
}
