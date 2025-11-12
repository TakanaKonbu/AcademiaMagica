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
import com.takanakonbu.academiamagica.model.PrestigeSkillState
import com.takanakonbu.academiamagica.model.PrestigeSkillType
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
                    // 超越スキル：マナ・ゴールド生産量ボーナス
                    val manaBoost = BigDecimal.ONE + (currentState.prestigeSkills[PrestigeSkillType.MANA_BOOST]?.level?.toBigDecimal()?.multiply(BigDecimal("0.1")) ?: BigDecimal.ZERO)
                    val goldBoost = BigDecimal.ONE + (currentState.prestigeSkills[PrestigeSkillType.GOLD_BOOST]?.level?.toBigDecimal()?.multiply(BigDecimal("0.1")) ?: BigDecimal.ZERO)

                    val botanyMultiplier = BigDecimal.ONE + (currentState.departments[DepartmentType.BOTANY]?.level?.toBigDecimal()?.multiply(BigDecimal("0.1")) ?: BigDecimal.ZERO)
                    val manaPerSecond = currentState.students.totalStudents.toBigDecimal().multiply(botanyMultiplier).multiply(manaBoost)
                    val goldPerSecond = currentState.students.totalStudents.toBigDecimal().multiply(botanyMultiplier).divide(BigDecimal(2), 2, RoundingMode.HALF_UP).multiply(goldBoost)

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

            if (departmentState.level >= currentState.maxDepartmentLevel) return@update currentState

            // 超越スキル：研究コスト割引
            val researchDiscount = BigDecimal.ONE - (currentState.prestigeSkills[PrestigeSkillType.RESEARCH_DISCOUNT]?.level?.toBigDecimal()?.multiply(BigDecimal("0.01")) ?: BigDecimal.ZERO)
            val libraryDiscount = BigDecimal.ONE - (currentState.facilities[FacilityType.DIMENSIONAL_LIBRARY]?.level?.toBigDecimal()?.multiply(BigDecimal("0.01")) ?: BigDecimal.ZERO)
            val cost = BigDecimal("1.5").pow(departmentState.level).multiply(BigDecimal(10)).multiply(libraryDiscount).multiply(researchDiscount).setScale(0, RoundingMode.CEILING)

            if (currentState.mana < cost) return@update currentState

            val newMana = currentState.mana.subtract(cost)
            val currentDepartments = currentState.departments.toMutableMap()
            currentDepartments[type] = departmentState.copy(level = departmentState.level + 1)

            currentState.copy(
                mana = newMana,
                departments = currentDepartments,
                totalMagicalPower = calculateTotalMagicalPower(currentState)
            )
        }
    }

    fun upgradeFacility(type: FacilityType) {
        _gameState.update { currentState ->
            val facilityState = currentState.facilities[type] ?: return@update currentState

            // 超越スキル：施設コスト割引
            val facilityDiscount = BigDecimal.ONE - (currentState.prestigeSkills[PrestigeSkillType.FACILITY_DISCOUNT]?.level?.toBigDecimal()?.multiply(BigDecimal("0.01")) ?: BigDecimal.ZERO)
            val cost = BigDecimal("2.0").pow(facilityState.level).multiply(BigDecimal(100)).multiply(facilityDiscount)

            if (currentState.gold < cost) return@update currentState

            val newGold = currentState.gold.subtract(cost)
            val currentFacilities = currentState.facilities.toMutableMap()
            currentFacilities[type] = facilityState.copy(level = facilityState.level + 1)

            currentState.copy(
                gold = newGold,
                facilities = currentFacilities,
                totalMagicalPower = calculateTotalMagicalPower(currentState)
            )
        }
    }

    fun recruitStudent() {
        _gameState.update { currentState ->
            val maxStudents = (currentState.facilities[FacilityType.GREAT_HALL]?.level ?: 0) * 10
            if (currentState.students.totalStudents >= maxStudents) return@update currentState

            val cost = BigDecimal("1.2").pow(currentState.students.totalStudents).multiply(BigDecimal(10))

            if (currentState.mana < cost) return@update currentState

            val newMana = currentState.mana.subtract(cost)
            val newStudents = currentState.students.copy(totalStudents = currentState.students.totalStudents + 1)

            currentState.copy(
                mana = newMana,
                students = newStudents,
                totalMagicalPower = calculateTotalMagicalPower(currentState)
            )
        }
    }

    fun upgradePrestigeSkill(type: PrestigeSkillType) {
        _gameState.update { currentState ->
            val skillState = currentState.prestigeSkills[type] ?: return@update currentState
            val cost = (skillState.level + 1).toLong()

            if (currentState.philosophersStones < cost) return@update currentState

            val newStones = currentState.philosophersStones - cost
            val currentSkills = currentState.prestigeSkills.toMutableMap()
            currentSkills[type] = skillState.copy(level = skillState.level + 1)

            currentState.copy(
                philosophersStones = newStones,
                prestigeSkills = currentSkills,
                totalMagicalPower = calculateTotalMagicalPower(currentState.copy(prestigeSkills = currentSkills)) // スキル更新後の状態で再計算
            )
        }
    }

    private fun calculateTotalMagicalPower(state: GameState): BigDecimal {
        val basePower = (state.departments[DepartmentType.ATTACK_MAGIC]?.level?.toBigDecimal()?.multiply(BigDecimal(10)) ?: BigDecimal.ZERO) + BigDecimal.ONE
        val studentBonus = BigDecimal.ONE + state.students.totalStudents.toBigDecimal().multiply(BigDecimal("0.1"))

        val facilityMultiplier = (BigDecimal("1.1").pow(state.facilities[FacilityType.GREAT_HALL]?.level ?: 0)).multiply(
            BigDecimal("1.1").pow(state.facilities[FacilityType.RESEARCH_WING]?.level ?: 0)
        )

        val departmentMultiplier = (BigDecimal.ONE + (state.departments[DepartmentType.BOTANY]?.level ?: 0).toBigDecimal().multiply(BigDecimal("0.05"))).multiply(
            (BigDecimal.ONE + (state.departments[DepartmentType.DEFENSE_MAGIC]?.level ?: 0).toBigDecimal().multiply(BigDecimal("0.05")))
        )

        // 賢者の石による直接的なボーナスは廃止
        val ancientMagicBonus = (BigDecimal.ONE + (state.departments[DepartmentType.ANCIENT_MAGIC]?.level?.toBigDecimal()?.multiply(BigDecimal("0.02")) ?: BigDecimal.ZERO))

        val totalPower = basePower
            .multiply(studentBonus)
            .multiply(facilityMultiplier)
            .multiply(departmentMultiplier)
            .multiply(ancientMagicBonus)

        return totalPower.setScale(2, RoundingMode.HALF_UP)
    }

    fun prestige() {
        _gameState.update { currentState ->
            // 超越スキル：賢者の石獲得量ボーナス
            val stoneBoost = 1.0 + (currentState.prestigeSkills[PrestigeSkillType.STONE_BOOST]?.level?.toDouble()?.times(0.05) ?: 0.0)
            val ancientMagicBonus = 1.0 + (currentState.departments[DepartmentType.ANCIENT_MAGIC]?.level?.toDouble()?.times(0.1) ?: 0.0)
            if (currentState.totalMagicalPower <= BigDecimal.ONE) return@update currentState
            val newStones = (Math.log10(currentState.totalMagicalPower.toDouble()) * ancientMagicBonus * stoneBoost).toLong()

            // ゲーム状態をリセットし、賢者の石と超越スキルレベルは引き継ぐ
            GameState(
                philosophersStones = currentState.philosophersStones + newStones,
                prestigeSkills = currentState.prestigeSkills
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
