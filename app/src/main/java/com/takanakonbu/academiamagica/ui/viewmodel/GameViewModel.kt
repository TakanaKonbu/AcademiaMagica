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

    companion object {
        // Offline Reward
        private const val BASE_OFFLINE_MINUTES = 60
        private const val OFFLINE_MINUTES_PER_SKILL_LEVEL = 10

        // Boost
        private const val BOOST_DURATION_SECONDS = 600

        // Department Upgrade
        private val DEPARTMENT_UPGRADE_COST_BASE = BigDecimal("1.5")
        private val DEPARTMENT_UPGRADE_COST_MULTIPLIER = BigDecimal(10)
        private val RESEARCH_DISCOUNT_PER_SKILL_LEVEL = BigDecimal("0.01")
        private val MAX_DISCOUNT = BigDecimal("0.9")

        // Facility Upgrade
        private val FACILITY_UPGRADE_COST_BASE = BigDecimal("2.0")
        private val FACILITY_UPGRADE_COST_MULTIPLIER = BigDecimal(100)
        private val FACILITY_DISCOUNT_PER_SKILL_LEVEL = BigDecimal("0.01")

        // Student Recruit
        private val RECRUIT_COST_BASE = BigDecimal("1.2")
        private val RECRUIT_COST_MULTIPLIER = BigDecimal(10)
        private const val MAX_STUDENTS_PER_GREAT_HALL_LEVEL = 10

        // Prestige Skill Upgrade
        private const val PRESTIGE_SKILL_MAX_LEVEL_OFFLINE_TIME = 18
        private const val PRESTIGE_SKILL_MAX_LEVEL_DISCOUNT = 90
        private val PRESTIGE_MAGICAL_POWER_BOOST_PER_LEVEL = BigDecimal("0.005")

        // Magical Power Calculation
        private val ATTACK_MAGIC_STUDENT_BONUS = BigDecimal(5)
        private val DEFENSE_MAGIC_STUDENT_BONUS_MULTIPLIER = BigDecimal("0.01")
        private val ATTACK_MAGIC_DEPARTMENT_BASE_POWER = BigDecimal(10)
        private val BASE_POWER_ADDITION = BigDecimal.ONE
        private val STUDENT_COUNT_BONUS_MULTIPLIER = BigDecimal("0.1")
        private val FACILITY_POWER_MULTIPLIER_BASE = BigDecimal("1.1")
        private val BOTANY_DEPARTMENT_MULTIPLIER_PER_LEVEL = BigDecimal("0.05")
        private val DEFENSE_MAGIC_DEPARTMENT_MULTIPLIER_PER_LEVEL = BigDecimal("0.05")
        private val ANCIENT_MAGIC_DEPARTMENT_POWER_BONUS = BigDecimal("0.02")
        private val DIMENSIONAL_LIBRARY_POWER_BONUS = BigDecimal("0.01")
        private const val MAGICAL_POWER_SCALE = 2

        // Prestige Calculation
        private const val PRESTIGE_ANCIENT_MAGIC_STUDENT_BONUS = 0.01
        private const val PRESTIGE_STONE_BOOST_BONUS = 0.05
        private const val PRESTIGE_ANCIENT_MAGIC_DEPARTMENT_BONUS = 0.1
    }

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
                val maxOfflineMinutes = BASE_OFFLINE_MINUTES + offlineTimeExtensionLevel * OFFLINE_MINUTES_PER_SKILL_LEVEL
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
            currentState.copy(boostRemainingSeconds = BOOST_DURATION_SECONDS)
        }
    }

    private fun updateStateWithPowerRecalculation(update: (currentState: GameState) -> GameState) {
        _gameState.update { currentState ->
            val newState = update(currentState)
            if (newState != currentState) {
                newState.copy(totalMagicalPower = calculateTotalMagicalPower(newState))
            } else {
                currentState
            }
        }
    }

    fun assignStudent(department: DepartmentType, amount: Int = 1) {
        updateStateWithPowerRecalculation { currentState ->
            if (currentState.students.unassignedStudents < amount) return@updateStateWithPowerRecalculation currentState
            val currentAssignments = currentState.students.specializedStudents.toMutableMap()
            currentAssignments[department] = (currentAssignments[department] ?: 0) + amount
            currentState.copy(students = currentState.students.copy(specializedStudents = currentAssignments))
        }
    }

    fun unassignStudent(department: DepartmentType, amount: Int = 1) {
        updateStateWithPowerRecalculation { currentState ->
            val assigned = currentState.students.specializedStudents[department] ?: 0
            if (assigned < amount) return@updateStateWithPowerRecalculation currentState
            val currentAssignments = currentState.students.specializedStudents.toMutableMap()
            currentAssignments[department] = assigned - amount
            currentState.copy(students = currentState.students.copy(specializedStudents = currentAssignments))
        }
    }

    fun upgradeDepartment(type: DepartmentType) {
        updateStateWithPowerRecalculation { currentState ->
            val departmentState = currentState.departments[type] ?: return@updateStateWithPowerRecalculation currentState
            if (departmentState.level >= currentState.maxDepartmentLevel) return@updateStateWithPowerRecalculation currentState

            val rawResearchDiscount = currentState.prestigeSkills[PrestigeSkillType.RESEARCH_DISCOUNT]?.level?.toBigDecimal()?.multiply(RESEARCH_DISCOUNT_PER_SKILL_LEVEL) ?: BigDecimal.ZERO
            val researchDiscount = BigDecimal.ONE - rawResearchDiscount.min(MAX_DISCOUNT)

            val cost = DEPARTMENT_UPGRADE_COST_BASE.pow(departmentState.level).multiply(DEPARTMENT_UPGRADE_COST_MULTIPLIER).multiply(researchDiscount).setScale(0, RoundingMode.CEILING)

            if (currentState.mana < cost) return@updateStateWithPowerRecalculation currentState

            val newMana = currentState.mana.subtract(cost)
            val currentDepartments = currentState.departments.toMutableMap()
            currentDepartments[type] = departmentState.copy(level = departmentState.level + 1)

            currentState.copy(
                mana = newMana,
                departments = currentDepartments
            )
        }
    }

    fun upgradeFacility(type: FacilityType) {
        updateStateWithPowerRecalculation { currentState ->
            val facilityState = currentState.facilities[type] ?: return@updateStateWithPowerRecalculation currentState

            val rawFacilityDiscount = currentState.prestigeSkills[PrestigeSkillType.FACILITY_DISCOUNT]?.level?.toBigDecimal()?.multiply(FACILITY_DISCOUNT_PER_SKILL_LEVEL) ?: BigDecimal.ZERO
            val facilityDiscount = BigDecimal.ONE - rawFacilityDiscount.min(MAX_DISCOUNT)
            val cost = FACILITY_UPGRADE_COST_BASE.pow(facilityState.level).multiply(FACILITY_UPGRADE_COST_MULTIPLIER).multiply(facilityDiscount)

            if (currentState.gold < cost) return@updateStateWithPowerRecalculation currentState

            val newGold = currentState.gold.subtract(cost)
            val currentFacilities = currentState.facilities.toMutableMap()
            currentFacilities[type] = facilityState.copy(level = facilityState.level + 1)

            currentState.copy(
                gold = newGold,
                facilities = currentFacilities
            )
        }
    }

    fun recruitStudent() {
        updateStateWithPowerRecalculation { currentState ->
            val maxStudents = (currentState.facilities[FacilityType.GREAT_HALL]?.level ?: 0) * MAX_STUDENTS_PER_GREAT_HALL_LEVEL
            if (currentState.students.totalStudents >= maxStudents) return@updateStateWithPowerRecalculation currentState

            val cost = RECRUIT_COST_BASE.pow(currentState.students.totalStudents).multiply(RECRUIT_COST_MULTIPLIER)

            if (currentState.mana < cost) return@updateStateWithPowerRecalculation currentState

            val newMana = currentState.mana.subtract(cost)
            val newStudents = currentState.students.copy(totalStudents = currentState.students.totalStudents + 1)

            currentState.copy(
                mana = newMana,
                students = newStudents
            )
        }
    }

    fun upgradePrestigeSkill(type: PrestigeSkillType) {
        updateStateWithPowerRecalculation { currentState ->
            val skillState = currentState.prestigeSkills[type] ?: return@updateStateWithPowerRecalculation currentState

            val maxLevel = when (type) {
                PrestigeSkillType.OFFLINE_TIME_EXTENSION -> PRESTIGE_SKILL_MAX_LEVEL_OFFLINE_TIME
                PrestigeSkillType.RESEARCH_DISCOUNT, PrestigeSkillType.FACILITY_DISCOUNT -> PRESTIGE_SKILL_MAX_LEVEL_DISCOUNT
                else -> null
            }

            if (maxLevel != null && skillState.level >= maxLevel) {
                return@updateStateWithPowerRecalculation currentState
            }

            val cost = (skillState.level + 1).toLong()

            if (currentState.philosophersStones < cost) return@updateStateWithPowerRecalculation currentState

            val newStones = currentState.philosophersStones - cost
            val currentSkills = currentState.prestigeSkills.toMutableMap()
            currentSkills[type] = skillState.copy(level = skillState.level + 1)

            currentState.copy(
                philosophersStones = newStones,
                prestigeSkills = currentSkills
            )
        }
    }

    private fun calculateTotalMagicalPower(state: GameState): BigDecimal {
        // --- 生徒配属によるボーナス ---
        val attackStudentBonus = state.students.specializedStudents[DepartmentType.ATTACK_MAGIC]?.toBigDecimal()?.multiply(ATTACK_MAGIC_STUDENT_BONUS) ?: BigDecimal.ZERO
        val defenseStudentBonus = BigDecimal.ONE + (state.students.specializedStudents[DepartmentType.DEFENSE_MAGIC]?.toBigDecimal()?.multiply(DEFENSE_MAGIC_STUDENT_BONUS_MULTIPLIER) ?: BigDecimal.ZERO)

        val basePower = (state.departments[DepartmentType.ATTACK_MAGIC]?.level?.toBigDecimal()?.multiply(ATTACK_MAGIC_DEPARTMENT_BASE_POWER) ?: BigDecimal.ZERO) + BASE_POWER_ADDITION + attackStudentBonus
        val studentBonus = BigDecimal.ONE + state.students.totalStudents.toBigDecimal().multiply(STUDENT_COUNT_BONUS_MULTIPLIER)

        val facilityMultiplier = (FACILITY_POWER_MULTIPLIER_BASE.pow(state.facilities[FacilityType.GREAT_HALL]?.level ?: 0)).multiply(
            FACILITY_POWER_MULTIPLIER_BASE.pow(state.facilities[FacilityType.RESEARCH_WING]?.level ?: 0)
        )

        val departmentMultiplier = (BigDecimal.ONE + (state.departments[DepartmentType.BOTANY]?.level ?: 0).toBigDecimal().multiply(BOTANY_DEPARTMENT_MULTIPLIER_PER_LEVEL)).multiply(
            (BigDecimal.ONE + (state.departments[DepartmentType.DEFENSE_MAGIC]?.level ?: 0).toBigDecimal().multiply(DEFENSE_MAGIC_DEPARTMENT_MULTIPLIER_PER_LEVEL))
        )

        val ancientMagicBonus = (BigDecimal.ONE + (state.departments[DepartmentType.ANCIENT_MAGIC]?.level?.toBigDecimal()?.multiply(ANCIENT_MAGIC_DEPARTMENT_POWER_BONUS) ?: BigDecimal.ZERO))

        val libraryMultiplier = BigDecimal.ONE + (state.facilities[FacilityType.DIMENSIONAL_LIBRARY]?.level?.toBigDecimal()?.multiply(DIMENSIONAL_LIBRARY_POWER_BONUS) ?: BigDecimal.ZERO)

        val magicalPowerBoostLevel = state.prestigeSkills[PrestigeSkillType.MAGICAL_POWER_BOOST]?.level ?: 0
        val magicalPowerBoost = (BigDecimal.ONE + PRESTIGE_MAGICAL_POWER_BOOST_PER_LEVEL).pow(magicalPowerBoostLevel)

        val totalPower = basePower
            .multiply(studentBonus)
            .multiply(facilityMultiplier)
            .multiply(departmentMultiplier)
            .multiply(defenseStudentBonus) // 防衛魔法科の生徒による乗算ボーナス
            .multiply(ancientMagicBonus)
            .multiply(libraryMultiplier)
            .multiply(magicalPowerBoost)

        return totalPower.setScale(MAGICAL_POWER_SCALE, RoundingMode.HALF_UP)
    }

    fun prestige() {
        _gameState.update { currentState ->
            // --- 生徒配属によるボーナス ---
            val ancientMagicStudentBonus = 1.0 + (currentState.students.specializedStudents[DepartmentType.ANCIENT_MAGIC]?.toDouble()?.times(PRESTIGE_ANCIENT_MAGIC_STUDENT_BONUS) ?: 0.0)

            // --- 超越スキルによるボーナス ---
            val stoneBoost = 1.0 + (currentState.prestigeSkills[PrestigeSkillType.STONE_BOOST]?.level?.toDouble()?.times(PRESTIGE_STONE_BOOST_BONUS) ?: 0.0)
            val ancientMagicBonus = 1.0 + (currentState.departments[DepartmentType.ANCIENT_MAGIC]?.level?.toDouble()?.times(PRESTIGE_ANCIENT_MAGIC_DEPARTMENT_BONUS) ?: 0.0)
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
