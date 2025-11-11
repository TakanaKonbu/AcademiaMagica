package com.takanakonbu.academiamagica.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takanakonbu.academiamagica.model.DepartmentType
import com.takanakonbu.academiamagica.model.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

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
                    currentState.copy(mana = currentState.mana.add(BigDecimal.ONE))
                }
                saveGame()
            }
        }
    }

    fun upgradeDepartment(type: DepartmentType) {
        _gameState.update { currentState ->
            val departmentState = currentState.departments[type] ?: return@update currentState
            val cost = BigDecimal(10).pow(departmentState.level)

            if (currentState.mana < cost) {
                return@update currentState
            }

            val newMana = currentState.mana.subtract(cost)

            val currentDepartments = currentState.departments.toMutableMap()
            currentDepartments[type] = departmentState.copy(level = departmentState.level + 1)

            currentState.copy(mana = newMana, departments = currentDepartments)
        }
    }

    fun saveGame() {
        // TODO: Implement save logic (e.g., using DataStore or SharedPreferences)
    }

    fun loadGame() {
        // TODO: Implement load logic
    }
}
