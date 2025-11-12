package com.takanakonbu.academiamagica.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.takanakonbu.academiamagica.model.DepartmentState
import com.takanakonbu.academiamagica.model.DepartmentType
import com.takanakonbu.academiamagica.model.FacilityState
import com.takanakonbu.academiamagica.model.FacilityType
import com.takanakonbu.academiamagica.model.GameState
import com.takanakonbu.academiamagica.model.StudentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.math.RoundingMode

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
                    // 生徒数と植物学レベルに基づきリソースを生成
                    val botanyMultiplier = BigDecimal.ONE + (currentState.departments[DepartmentType.BOTANY]?.level?.toBigDecimal()?.multiply(BigDecimal("0.1")) ?: BigDecimal.ZERO)
                    val manaPerSecond = currentState.students.totalStudents.toBigDecimal().multiply(botanyMultiplier)
                    val goldPerSecond = currentState.students.totalStudents.toBigDecimal().multiply(botanyMultiplier).divide(BigDecimal(2), 2, RoundingMode.HALF_UP) // ゴールドはマナの半分

                    currentState.copy(
                        mana = currentState.mana.add(manaPerSecond),
                        gold = currentState.gold.add(goldPerSecond)
                    )
                }
                saveGame()
            }
        }
    }

    fun upgradeDepartment(type: DepartmentType) {
        _gameState.update { currentState ->
            val departmentState = currentState.departments[type] ?: return@update currentState

            // GameStateから最大レベルを取得し、上限に達しているかチェック
            if (departmentState.level >= currentState.maxDepartmentLevel) return@update currentState

            // 次元図書館によるコスト削減
            val libraryDiscount = BigDecimal.ONE - (currentState.facilities[FacilityType.DIMENSIONAL_LIBRARY]?.level?.toBigDecimal()?.multiply(BigDecimal("0.01")) ?: BigDecimal.ZERO)
            val cost = BigDecimal("1.5").pow(departmentState.level).multiply(BigDecimal(10)).multiply(libraryDiscount).setScale(0, RoundingMode.CEILING)

            if (currentState.mana < cost) return@update currentState

            val newMana = currentState.mana.subtract(cost)
            val currentDepartments = currentState.departments.toMutableMap()
            currentDepartments[type] = departmentState.copy(level = departmentState.level + 1)

            currentState.copy(
                mana = newMana,
                departments = currentDepartments,
                totalMagicalPower = calculateTotalMagicalPower(currentDepartments, currentState.facilities, currentState.students, currentState.philosophersStones)
            )
        }
    }

    fun upgradeFacility(type: FacilityType) {
        _gameState.update { currentState ->
            val facilityState = currentState.facilities[type] ?: return@update currentState
            val cost = BigDecimal("2.0").pow(facilityState.level).multiply(BigDecimal(100))

            if (currentState.gold < cost) return@update currentState

            val newGold = currentState.gold.subtract(cost)
            val currentFacilities = currentState.facilities.toMutableMap()
            currentFacilities[type] = facilityState.copy(level = facilityState.level + 1)

            currentState.copy(
                gold = newGold,
                facilities = currentFacilities,
                totalMagicalPower = calculateTotalMagicalPower(currentState.departments, currentFacilities, currentState.students, currentState.philosophersStones)
            )
        }
    }

    fun recruitStudent() {
        _gameState.update { currentState ->
            // 大講堂レベルを上限とする
            val maxStudents = (currentState.facilities[FacilityType.GREAT_HALL]?.level ?: 0) * 10
            if (currentState.students.totalStudents >= maxStudents) return@update currentState

            val cost = BigDecimal("1.2").pow(currentState.students.totalStudents).multiply(BigDecimal(10))

            if (currentState.mana < cost) return@update currentState

            val newMana = currentState.mana.subtract(cost)
            val newStudents = currentState.students.copy(totalStudents = currentState.students.totalStudents + 1)

            currentState.copy(
                mana = newMana,
                students = newStudents,
                totalMagicalPower = calculateTotalMagicalPower(currentState.departments, currentState.facilities, newStudents, currentState.philosophersStones)
            )
        }
    }

    private fun calculateTotalMagicalPower(departments: Map<DepartmentType, DepartmentState>, facilities: Map<FacilityType, FacilityState>, students: StudentState, stones: Long): BigDecimal {
        val basePower = (departments[DepartmentType.ATTACK_MAGIC]?.level?.toBigDecimal()?.multiply(BigDecimal(10)) ?: BigDecimal.ZERO) + BigDecimal.ONE
        val studentBonus = BigDecimal.ONE + students.totalStudents.toBigDecimal().multiply(BigDecimal("0.1"))

        // 施設乗数: 大講堂と研究棟の効果
        val facilityMultiplier = (BigDecimal("1.1").pow(facilities[FacilityType.GREAT_HALL]?.level ?: 0)).multiply(
            BigDecimal("1.1").pow(facilities[FacilityType.RESEARCH_WING]?.level ?: 0)
        )

        // 学科乗数: 植物学と防衛魔法の効果
        val departmentMultiplier = (BigDecimal.ONE + (departments[DepartmentType.BOTANY]?.level ?: 0).toBigDecimal().multiply(BigDecimal("0.05"))).multiply(
            (BigDecimal.ONE + (departments[DepartmentType.DEFENSE_MAGIC]?.level ?: 0).toBigDecimal().multiply(BigDecimal("0.05")))
        )

        val ancientMagicBonus = (BigDecimal.ONE + (departments[DepartmentType.ANCIENT_MAGIC]?.level?.toBigDecimal()?.multiply(BigDecimal("0.02")) ?: BigDecimal.ZERO))
        val prestigeBonus = (BigDecimal.ONE + stones.toBigDecimal().multiply(BigDecimal("0.1"))).multiply(ancientMagicBonus)

        val totalPower = basePower
            .multiply(studentBonus)
            .multiply(facilityMultiplier)
            .multiply(departmentMultiplier)
            .multiply(prestigeBonus)

        return totalPower.setScale(2, RoundingMode.HALF_UP)
    }

    fun prestige() {
        _gameState.update { currentState ->
            // 古代魔術レベルに応じて獲得量増加
            val ancientMagicBonus = 1.0 + (currentState.departments[DepartmentType.ANCIENT_MAGIC]?.level?.toDouble()?.times(0.1) ?: 0.0)
            if (currentState.totalMagicalPower <= BigDecimal.ONE) return@update currentState
            val newStones = (Math.log10(currentState.totalMagicalPower.toDouble()) * ancientMagicBonus).toLong()

            GameState(philosophersStones = currentState.philosophersStones + newStones)
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
