package com.takanakonbu.academiamagica.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takanakonbu.academiamagica.model.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState

    init {
        loadGame()
        startGameLoop()
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (true) {
                delay(1000) // 1秒待機
                _gameState.update { currentState ->
                    currentState.copy(mana = currentState.mana.add(java.math.BigDecimal.ONE))
                }
                saveGame()
            }
        }
    }

    fun saveGame() {
        // TODO: Implement save logic (e.g., using DataStore or SharedPreferences)
    }

    fun loadGame() {
        // TODO: Implement load logic
    }
}
