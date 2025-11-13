package com.takanakonbu.academiamagica.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.takanakonbu.academiamagica.model.DepartmentType
import com.takanakonbu.academiamagica.model.GameState
import com.takanakonbu.academiamagica.model.PrestigeSkillState
import com.takanakonbu.academiamagica.model.PrestigeSkillType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.math.RoundingMode

private val Application.dataStore by preferencesDataStore(name = "game_state")

data class OfflineRewardState(
    val minutes: Long = 0,
    val manaGained: BigDecimal = BigDecimal.ZERO,
    val goldGained: BigDecimal = BigDecimal.ZERO
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _offlineRewardState = MutableStateFlow<OfflineRewardState?>(null)
    val offlineRewardState: StateFlow<OfflineRewardState?> = _offlineRewardState.asStateFlow()

    private val gameStateKey = stringPreferencesKey("game_state_json")

    private var gameLoopJob: Job? = null

    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadGame()
    }

    fun onAppForegrounded() {
        viewModelScope.launch {
            _isLoading.first { !it }

            if (gameLoopJob?.isActive == true) {
                return@launch
            }

            val now = System.currentTimeMillis()
            val elapsedSeconds = (now - gameState.value.lastOnlineTimestamp) / 1000
            val offlineMinutes = elapsedSeconds / 60

            if (offlineMinutes > 0) {
                val offlineTimeExtensionLevel = _gameState.value.prestigeSkills[PrestigeSkillType.OFFLINE_TIME_EXTENSION]?.level ?: 0
                val maxOfflineMinutes = 60 + offlineTimeExtensionLevel * 10
                val cappedMinutes = offlineMinutes.coerceAtMost(maxOfflineMinutes.toLong())
                val manaGained = gameState.value.manaPerSecond.multiply(BigDecimal(cappedMinutes * 60))
                val goldGained = gameState.value.goldPerSecond.multiply(BigDecimal(cappedMinutes * 60))
                _offlineRewardState.value = OfflineRewardState(cappedMinutes, manaGained, goldGained)
            } else {
                startGameLoop()
            }
        }
    }

    fun onAppBackgrounded() {
        stopGameLoop()
        saveGame()
    }

    fun dismissOfflineRewardDialog() {
        val reward = _offlineRewardState.value ?: return
        _gameState.update { currentState ->
            currentState.copy(
                mana = currentState.mana.add(reward.manaGained),
                gold = currentState.gold.add(reward.goldGained)
            )
        }
        _offlineRewardState.value = null
        startGameLoop()
    }

    fun doubleOfflineReward() {
        // TODO: リワード広告を実装
        val reward = _offlineRewardState.value ?: return
        _gameState.update { currentState ->
            currentState.copy(
                mana = currentState.mana.add(reward.manaGained.multiply(BigDecimal(2))),
                gold = currentState.gold.add(reward.goldGained.multiply(BigDecimal(2)))
            )
        }
        _offlineRewardState.value = null
        startGameLoop()
    }

    fun startGameLoop() {
        if (gameLoopJob?.isActive == true) return
        gameLoopJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                _gameState.update { currentState ->
                    val newBoostTime = if (currentState.boostRemainingSeconds > 0) currentState.boostRemainingSeconds - 1 else 0
                    currentState.copy(
                        mana = currentState.mana.add(currentState.manaPerSecond),
                        gold = currentState.gold.add(currentState.goldPerSecond),
                        boostRemainingSeconds = newBoostTime,
                        lastOnlineTimestamp = System.currentTimeMillis()
                    )
                }
                saveGame()
            }
        }
    }

    fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    fun setSchoolName(name: String) {
        _gameState.update { it.copy(schoolName = name) }
    }

    private fun loadGame() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stateJson = getApplication<Application>().dataStore.data.first()[gameStateKey]
                if (stateJson != null) {
                    var loadedState = json.decodeFromString<GameState>(stateJson)
                    val missingSkills = PrestigeSkillType.values().toSet() - loadedState.prestigeSkills.keys
                    if (missingSkills.isNotEmpty()) {
                        val newSkills = loadedState.prestigeSkills.toMutableMap()
                        missingSkills.forEach { skill ->
                            newSkills[skill] = PrestigeSkillState()
                        }
                        loadedState = loadedState.copy(prestigeSkills = newSkills)
                    }
                    _gameState.value = loadedState
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveGame() {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit {
                it[gameStateKey] = json.encodeToString(gameState.value)
            }
        }
    }

    fun doubleManaAndGold() {
        // TODO: リワード広告を実装する
        _gameState.update { currentState ->
            currentState.copy(
                mana = currentState.mana.multiply(BigDecimal(2)),
                gold = currentState.gold.multiply(BigDecimal(2))
            )
        }
    }

    fun startBoost() {
        // TODO: リワード広告を実装する
        _gameState.update { currentState ->
            currentState.copy(boostRemainingSeconds = 600) // 10分 = 600秒
        }
    }

    fun assignStudent(department: DepartmentType, amount: Int = 1) {
        _gameState.update { currentState ->
            if (currentState.students.unassignedStudents < amount) return@update currentState
            val currentAssignments = currentState.students.specializedStudents.toMutableMap()
            currentAssignments[department] = (currentAssignments[department] ?: 0) + amount
            val updatedState = currentState.copy(students = currentState.students.copy(specializedStudents = currentAssignments))
            updatedState.copy(totalMagicalPower = calculateTotalMagicalPower(updatedState))
        }
    }

    fun unassignStudent(department: DepartmentType, amount: Int = 1) {
        _gameState.update { currentState ->
            val assigned = currentState.students.specializedStudents[department] ?: 0
            if (assigned < amount) return@update currentState
            val currentAssignments = currentState.students.specializedStudents.toMutableMap()
            currentAssignments[department] = assigned - amount
            val updatedState = currentState.copy(students = currentState.students.copy(specializedStudents = currentAssignments))
            updatedState.copy(totalMagicalPower = calculateTotalMagicalPower(updatedState))
        }
    }

    fun upgradeDepartment(type: DepartmentType) {
        _gameState.update { currentState ->
            val departmentState = currentState.departments[type] ?: return@update currentState
            if (departmentState.level >= currentState.maxDepartmentLevel) return@update currentState

            val researchDiscount = BigDecimal.ONE - (currentState.prestigeSkills[PrestigeSkillType.RESEARCH_DISCOUNT]?.level?.toBigDecimal()?.multiply(BigDecimal("0.01")) ?: BigDecimal.ZERO)
            val libraryDiscount = BigDecimal.ONE - (currentState.facilities[com.takanakonbu.academiamagica.model.FacilityType.DIMENSIONAL_LIBRARY]?.level?.toBigDecimal()?.multiply(BigDecimal("0.01")) ?: BigDecimal.ZERO)
            val cost = BigDecimal("1.5").pow(departmentState.level).multiply(BigDecimal(10)).multiply(libraryDiscount).multiply(researchDiscount).setScale(0, RoundingMode.CEILING)

            if (currentState.mana < cost) return@update currentState

            val newMana = currentState.mana.subtract(cost)
            val currentDepartments = currentState.departments.toMutableMap()
            currentDepartments[type] = departmentState.copy(level = departmentState.level + 1)

            val updatedState = currentState.copy(
                mana = newMana,
                departments = currentDepartments
            )
            updatedState.copy(totalMagicalPower = calculateTotalMagicalPower(updatedState))
        }
    }

    fun upgradeFacility(type: com.takanakonbu.academiamagica.model.FacilityType) {
        _gameState.update { currentState ->
            val facilityState = currentState.facilities[type] ?: return@update currentState

            val facilityDiscount = BigDecimal.ONE - (currentState.prestigeSkills[PrestigeSkillType.FACILITY_DISCOUNT]?.level?.toBigDecimal()?.multiply(BigDecimal("0.01")) ?: BigDecimal.ZERO)
            val cost = BigDecimal("2.0").pow(facilityState.level).multiply(BigDecimal(100)).multiply(facilityDiscount)

            if (currentState.gold < cost) return@update currentState

            val newGold = currentState.gold.subtract(cost)
            val currentFacilities = currentState.facilities.toMutableMap()
            currentFacilities[type] = facilityState.copy(level = facilityState.level + 1)

            val updatedState = currentState.copy(
                gold = newGold,
                facilities = currentFacilities
            )
            updatedState.copy(totalMagicalPower = calculateTotalMagicalPower(updatedState))
        }
    }

    fun recruitStudent() {
        _gameState.update { currentState ->
            val maxStudents = (currentState.facilities[com.takanakonbu.academiamagica.model.FacilityType.GREAT_HALL]?.level ?: 0) * 10
            if (currentState.students.totalStudents >= maxStudents) return@update currentState

            val cost = BigDecimal("1.2").pow(currentState.students.totalStudents).multiply(BigDecimal(10))

            if (currentState.mana < cost) return@update currentState

            val newMana = currentState.mana.subtract(cost)
            val newStudents = currentState.students.copy(totalStudents = currentState.students.totalStudents + 1)

            val updatedState = currentState.copy(
                mana = newMana,
                students = newStudents
            )
            updatedState.copy(totalMagicalPower = calculateTotalMagicalPower(updatedState))
        }
    }

    fun upgradePrestigeSkill(type: PrestigeSkillType) {
        _gameState.update { currentState ->
            val skillState = currentState.prestigeSkills[type] ?: return@update currentState

            if (type == PrestigeSkillType.OFFLINE_TIME_EXTENSION && skillState.level >= 18) {
                return@update currentState
            }

            val cost = (skillState.level + 1).toLong()

            if (currentState.philosophersStones < cost) return@update currentState

            val newStones = currentState.philosophersStones - cost
            val currentSkills = currentState.prestigeSkills.toMutableMap()
            currentSkills[type] = skillState.copy(level = skillState.level + 1)

            val updatedState = currentState.copy(
                philosophersStones = newStones,
                prestigeSkills = currentSkills
            )
            updatedState.copy(totalMagicalPower = calculateTotalMagicalPower(updatedState))
        }
    }

    private fun calculateTotalMagicalPower(state: GameState): BigDecimal {
        // --- 生徒配属によるボーナス ---
        val attackStudentBonus = state.students.specializedStudents[DepartmentType.ATTACK_MAGIC]?.toBigDecimal()?.multiply(BigDecimal(5)) ?: BigDecimal.ZERO
        val defenseStudentBonus = BigDecimal.ONE + (state.students.specializedStudents[DepartmentType.DEFENSE_MAGIC]?.toBigDecimal()?.multiply(BigDecimal("0.01")) ?: BigDecimal.ZERO)

        val basePower = (state.departments[DepartmentType.ATTACK_MAGIC]?.level?.toBigDecimal()?.multiply(BigDecimal(10)) ?: BigDecimal.ZERO) + BigDecimal.ONE + attackStudentBonus
        val studentBonus = BigDecimal.ONE + state.students.totalStudents.toBigDecimal().multiply(BigDecimal("0.1"))

        val facilityMultiplier = (BigDecimal("1.1").pow(state.facilities[com.takanakonbu.academiamagica.model.FacilityType.GREAT_HALL]?.level ?: 0)).multiply(
            BigDecimal("1.1").pow(state.facilities[com.takanakonbu.academiamagica.model.FacilityType.RESEARCH_WING]?.level ?: 0)
        )

        val departmentMultiplier = (BigDecimal.ONE + (state.departments[DepartmentType.BOTANY]?.level ?: 0).toBigDecimal().multiply(BigDecimal("0.05"))).multiply(
            (BigDecimal.ONE + (state.departments[DepartmentType.DEFENSE_MAGIC]?.level ?: 0).toBigDecimal().multiply(BigDecimal("0.05")))
        )

        val ancientMagicBonus = (BigDecimal.ONE + (state.departments[DepartmentType.ANCIENT_MAGIC]?.level?.toBigDecimal()?.multiply(BigDecimal("0.02")) ?: BigDecimal.ZERO))

        val totalPower = basePower
            .multiply(studentBonus)
            .multiply(facilityMultiplier)
            .multiply(departmentMultiplier)
            .multiply(defenseStudentBonus) // 防衛魔法科の生徒による乗算ボーナス
            .multiply(ancientMagicBonus)

        return totalPower.setScale(2, RoundingMode.HALF_UP)
    }

    fun prestige() {
        _gameState.update { currentState ->
            // --- 生徒配属によるボーナス ---
            val ancientMagicStudentBonus = 1.0 + (currentState.students.specializedStudents[DepartmentType.ANCIENT_MAGIC]?.toDouble()?.times(0.01) ?: 0.0)

            // --- 超越スキルによるボーナス ---
            val stoneBoost = 1.0 + (currentState.prestigeSkills[PrestigeSkillType.STONE_BOOST]?.level?.toDouble()?.times(0.05) ?: 0.0)
            val ancientMagicBonus = 1.0 + (currentState.departments[com.takanakonbu.academiamagica.model.DepartmentType.ANCIENT_MAGIC]?.level?.toDouble()?.times(0.1) ?: 0.0)
            if (currentState.totalMagicalPower <= BigDecimal.ONE) return@update currentState
            val newStones = (Math.log10(currentState.totalMagicalPower.toDouble()) * ancientMagicBonus * stoneBoost * ancientMagicStudentBonus).toLong()

            GameState(
                schoolName = currentState.schoolName, // 学校名を引き継ぐ
                philosophersStones = currentState.philosophersStones + newStones,
                prestigeSkills = currentState.prestigeSkills
            )
        }
    }
}
