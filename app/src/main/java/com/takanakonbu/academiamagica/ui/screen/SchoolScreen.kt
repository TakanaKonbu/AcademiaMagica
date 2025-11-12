package com.takanakonbu.academiamagica.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takanakonbu.academiamagica.model.DepartmentType
import com.takanakonbu.academiamagica.model.FacilityType
import com.takanakonbu.academiamagica.ui.common.OverallPowerCard
import com.takanakonbu.academiamagica.ui.common.UpgradeItemCard
import com.takanakonbu.academiamagica.ui.common.formatInflationNumber
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun SchoolScreen(gameViewModel: GameViewModel, paddingValues: PaddingValues) {
    val gameState by gameViewModel.gameState.collectAsState()

    val botanyMultiplier = BigDecimal.ONE + (gameState.departments[DepartmentType.BOTANY]?.level?.toBigDecimal()?.multiply(BigDecimal("0.1")) ?: BigDecimal.ZERO)
    val manaPerSecond = gameState.students.totalStudents.toBigDecimal().multiply(botanyMultiplier)
    val goldPerSecond = manaPerSecond.divide(BigDecimal(2), 2, RoundingMode.HALF_UP)

    LazyColumn(modifier = Modifier.padding(paddingValues)) {
        item {
            val maxStudents = (gameState.facilities[FacilityType.GREAT_HALL]?.level ?: 0) * 10
            OverallPowerCard(
                totalMagicalPower = gameState.totalMagicalPower,
                currentMana = gameState.mana,
                manaPerSecond = manaPerSecond,
                currentGold = gameState.gold,
                goldPerSecond = goldPerSecond,
                totalStudents = gameState.students.totalStudents,
                maxStudents = maxStudents,
                philosophersStones = gameState.philosophersStones
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- 生徒カテゴリ ---
        item { Text("🏫 運営", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(4.dp)) }
        item {
            val maxStudents = (gameState.facilities[FacilityType.GREAT_HALL]?.level ?: 0) * 10
            val cost = BigDecimal("1.2").pow(gameState.students.totalStudents).multiply(BigDecimal(10))
            UpgradeItemCard(
                name = "🧑‍🎓 生徒募集",
                level = gameState.students.totalStudents,
                maxLevel = maxStudents,
                effect = "マナとゴールドの基本生産量を増加させる",
                costText = "募集 (マナ: ${formatInflationNumber(cost)})",
                isEnabled = gameState.mana >= cost && gameState.students.totalStudents < maxStudents,
                onUpgrade = { gameViewModel.recruitStudent() }
            )
        }

        // --- 周回カテゴリ ---
        item { Spacer(Modifier.height(16.dp)); Text("✨ 超越", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(4.dp)) }
        item {
            val ancientMagicBonus = 1.0 + (gameState.departments[DepartmentType.ANCIENT_MAGIC]?.level?.toDouble()?.times(0.1) ?: 0.0)
            val newStones = if (gameState.totalMagicalPower <= BigDecimal.ONE) 0 else (Math.log10(gameState.totalMagicalPower.toDouble()) * ancientMagicBonus).toLong()
            UpgradeItemCard(
                name = "💫 周回リセット",
                level = gameState.philosophersStones.toInt(),
                effect = "世界をリセットし、総合魔力に応じた賢者の石を獲得する。(${formatInflationNumber(gameState.totalMagicalPower)} -> $newStones 石)",
                costText = "実行",
                isEnabled = newStones > 0,
                onUpgrade = { gameViewModel.prestige() }
            )
        }
    }
}
