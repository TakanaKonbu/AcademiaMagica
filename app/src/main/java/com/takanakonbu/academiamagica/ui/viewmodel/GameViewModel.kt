package com.takanakonbu.academiamagica.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.takanakonbu.academiamagica.model.DepartmentType
import com.takanakonbu.academiamagica.model.FacilityType
import com.takanakonbu.academiamagica.model.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal

private val Application.dataStore by preferencesDataStore(name = "game_state")

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState

    private val gameStateKey = stringPreferencesKey("game_state_json")

    init {
        loadGame()
        startGameLoop()
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000) // 1秒待機
                _gameState.update { currentState ->
                    val manaPerSecond = BigDecimal.ONE.add(BigDecimal(currentState.philosophersStones))
                    currentState.copy(mana = currentState.mana.add(manaPerSecond))
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

            val newTotalMagicalPower = calculateTotalMagicalPower(currentDepartments, currentState.facilities)

            currentState.copy(
                mana = newMana,
                departments = currentDepartments,
                totalMagicalPower = newTotalMagicalPower
            )
        }
    }

    fun upgradeFacility(type: FacilityType) {
        _gameState.update { currentState ->
            val facilityState = currentState.facilities[type] ?: return@update currentState
            // コスト計算式は学科と少し変えておく
            val cost = BigDecimal(20).pow(facilityState.level)

            if (currentState.mana < cost) {
                return@update currentState
            }

            val newMana = currentState.mana.subtract(cost)

            val currentFacilities = currentState.facilities.toMutableMap()
            currentFacilities[type] = facilityState.copy(level = facilityState.level + 1)

            val newTotalMagicalPower = calculateTotalMagicalPower(currentState.departments, currentFacilities)

            currentState.copy(
                mana = newMana,
                facilities = currentFacilities,
                totalMagicalPower = newTotalMagicalPower
            )
        }
    }

    private fun calculateTotalMagicalPower(departments: Map<DepartmentType, com.takanakonbu.academiamagica.model.DepartmentState>, facilities: Map<FacilityType, com.takanakonbu.academiamagica.model.FacilityState>): BigDecimal {
        val totalDepartmentLevel = departments.values.sumOf { it.level }
        val totalFacilityLevel = facilities.values.sumOf { it.level }
        return BigDecimal((totalDepartmentLevel + totalFacilityLevel) * 10)
    }

    fun prestige() {
        _gameState.update { currentState ->
            val totalLevels = currentState.departments.values.sumOf { it.level } + currentState.facilities.values.sumOf { it.level }
            val newStones = totalLevels / 10 // 仮：合計レベル10ごとに1つの石

            GameState(
                philosophersStones = currentState.philosophersStones + newStones
            )
        }
    }

    fun saveGame() {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit {
                it[gameStateKey] = Json.encodeToString(gameState.value)
            }
        }
    }

    fun loadGame() {
        viewModelScope.launch {
            val stateJson = getApplication<Application>().dataStore.data.first()[gameStateKey]
            if (stateJson != null) {
                _gameState.value = Json.decodeFromString<GameState>(stateJson)
            }
        }
    }
}
