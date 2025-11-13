package com.takanakonbu.academiamagica.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.takanakonbu.academiamagica.ui.common.ActionButtons
import com.takanakonbu.academiamagica.ui.common.OverallPowerCard
import com.takanakonbu.academiamagica.ui.common.UpgradeItemCard
import com.takanakonbu.academiamagica.ui.common.formatInflationNumber
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel
import java.math.BigDecimal
import java.math.RoundingMode

private fun DepartmentType.toJapanese(): String = when (this) {
    DepartmentType.ATTACK_MAGIC -> "🔥 攻撃魔法"
    DepartmentType.BOTANY -> "🌿 植物学"
    DepartmentType.DEFENSE_MAGIC -> "🛡️ 防衛魔法"
    DepartmentType.ANCIENT_MAGIC -> "📖 古代魔術"
}

@Composable
fun DepartmentScreen(gameViewModel: GameViewModel, paddingValues: PaddingValues) {
    val gameState by gameViewModel.gameState.collectAsState()

    LazyColumn(modifier = Modifier.padding(paddingValues)) {
        item {
            val maxStudents = (gameState.facilities[FacilityType.GREAT_HALL]?.level ?: 0) * 10
            OverallPowerCard(
                totalMagicalPower = gameState.totalMagicalPower,
                currentMana = gameState.mana,
                manaPerSecond = gameState.manaPerSecond, // 修正
                currentGold = gameState.gold,
                goldPerSecond = gameState.goldPerSecond, // 修正
                totalStudents = gameState.students.totalStudents,
                maxStudents = maxStudents,
                philosophersStones = gameState.philosophersStones
            )
            Spacer(modifier = Modifier.height(16.dp))
            ActionButtons(gameViewModel = gameViewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- 学科カテゴリ ---
        item { Spacer(Modifier.height(16.dp)); Text("📚 学科", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(4.dp)) }
        items(gameState.departments.entries.toList()) { (type, state) ->
            val libraryDiscount = BigDecimal.ONE - (gameState.facilities[FacilityType.DIMENSIONAL_LIBRARY]?.level?.toBigDecimal()?.multiply(BigDecimal("0.01")) ?: BigDecimal.ZERO)
            val cost = BigDecimal("1.5").pow(state.level).multiply(BigDecimal(10)).multiply(libraryDiscount).setScale(0, RoundingMode.CEILING)
            // GameStateから算出プロパティとして最大レベルを取得
            val maxLevel = gameState.maxDepartmentLevel
            val effectText = when(type) {
                DepartmentType.ATTACK_MAGIC -> "総合魔力の基本値を+10増加させる"
                DepartmentType.BOTANY -> "マナとゴールドの生産量に+10%の乗算ボーナス"
                DepartmentType.DEFENSE_MAGIC -> "総合魔力に+5%の乗算ボーナス"
                DepartmentType.ANCIENT_MAGIC -> "周回時の賢者の石獲得量に+2%ボーナス"
            }
            UpgradeItemCard(
                name = type.toJapanese(),
                level = state.level,
                maxLevel = maxLevel,
                effect = effectText,
                costText = "研究 (マナ: ${formatInflationNumber(cost)})",
                isEnabled = gameState.mana >= cost && state.level < maxLevel,
                onUpgrade = { gameViewModel.upgradeDepartment(type) }
            )
        }
    }
}
