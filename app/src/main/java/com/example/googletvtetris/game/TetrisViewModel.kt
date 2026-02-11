package com.example.googletvtetris.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TetrisViewModel : ViewModel() {
    private var seedCounter = 1
    private val _state = MutableStateFlow(initialState(seedCounter++))
    val state: StateFlow<TetrisState> = _state

    init {
        viewModelScope.launch {
            while (true) {
                val current = _state.value
                delay(dropIntervalMs(current.level, current.softDropping))
                _state.update { reduce(it, GameAction.Tick, seedCounter++) }
            }
        }
    }

    fun dispatch(action: GameAction) {
        _state.update { reduce(it, action, seedCounter++) }
    }
}
