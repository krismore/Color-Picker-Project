package com.example.colorproject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RGBState(
    val red: Float = 1f,
    val green: Float = 1f,
    val blue: Float = 1f,
    val redEnabled: Boolean = true,
    val greenEnabled: Boolean = true,
    val blueEnabled: Boolean = true
)

class ColorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val colorDataStore: ColorDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(RGBState())
    val state: StateFlow<RGBState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            colorDataStore.loadRGBState().collect { saved ->
                _state.value = saved
            }
        }
    }

    fun setColorComponent(component: String, value: Float) {
        _state.update {
            when (component) {
                "red" -> it.copy(red = value)
                "green" -> it.copy(green = value)
                "blue" -> it.copy(blue = value)
                else -> it
            }
        }
    }

    fun toggleComponent(component: String, enabled: Boolean) {
        _state.update {
            when (component) {
                "red" -> it.copy(redEnabled = enabled)
                "green" -> it.copy(greenEnabled = enabled)
                "blue" -> it.copy(blueEnabled = enabled)
                else -> it
            }
        }
    }

    fun resetColors() {
        _state.value = RGBState()
        saveState()
    }

    fun saveState() {
        viewModelScope.launch {
            colorDataStore.saveRGBState(_state.value)
        }
    }
}
