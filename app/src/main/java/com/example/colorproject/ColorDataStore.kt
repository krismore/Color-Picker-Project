package com.example.colorproject

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "color_prefs"

private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

class ColorDataStore(private val context: Context) {

    private val RED_KEY = floatPreferencesKey("red_value")
    private val GREEN_KEY = floatPreferencesKey("green_value")
    private val BLUE_KEY = floatPreferencesKey("blue_value")
    private val RED_ENABLED_KEY = booleanPreferencesKey("red_enabled")
    private val GREEN_ENABLED_KEY = booleanPreferencesKey("green_enabled")
    private val BLUE_ENABLED_KEY = booleanPreferencesKey("blue_enabled")

    suspend fun saveRGBState(state: RGBState) {
        context.dataStore.edit { prefs ->
            prefs[RED_KEY] = state.red
            prefs[GREEN_KEY] = state.green
            prefs[BLUE_KEY] = state.blue
            prefs[RED_ENABLED_KEY] = state.redEnabled
            prefs[GREEN_ENABLED_KEY] = state.greenEnabled
            prefs[BLUE_ENABLED_KEY] = state.blueEnabled
        }
    }

    fun loadRGBState(): Flow<RGBState> {
        return context.dataStore.data.map { prefs ->
            RGBState(
                red = prefs[RED_KEY] ?: 1f,
                green = prefs[GREEN_KEY] ?: 1f,
                blue = prefs[BLUE_KEY] ?: 1f,
                redEnabled = prefs[RED_ENABLED_KEY] ?: true,
                greenEnabled = prefs[GREEN_ENABLED_KEY] ?: true,
                blueEnabled = prefs[BLUE_ENABLED_KEY] ?: true
            )
        }
    }
}
