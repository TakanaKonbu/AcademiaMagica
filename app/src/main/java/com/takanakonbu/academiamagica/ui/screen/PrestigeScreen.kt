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
import com.takanakonbu.academiamagica.model.PrestigeSkillType
import com.takanakonbu.academiamagica.ui.common.ActionButtons
import com.takanakonbu.academiamagica.ui.common.OverallPowerCard
import com.takanakonbu.academiamagica.ui.common.UpgradeItemCard
import com.takanakonbu.academiamagica.ui.common.formatInflationNumber
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel
import java.math.BigDecimal

private fun PrestigeSkillType.toJapanese(): String = when (this) {
    PrestigeSkillType.MANA_BOOST -> "✨ マナ生産量ボーナス"
    PrestigeSkillType.GOLD_BOOST -> "💰 ゴールド生産量ボーナス"
    PrestigeSkillType.RESEARCH_DISCOUNT -> "📚 学科研究コスト割引"
    PrestigeSkillType.FACILITY_DISCOUNT -> "🏰 施設改築コスト割引"
    PrestigeSkillType.STONE_BOOST -> "💎 賢者の石獲得量ボーナス"
    PrestigeSkillType.OFFLINE_TIME_EXTENSION -> "⏳ 放置可能時間延長"
    PrestigeSkillType.MAGICAL_POWER_BOOST -> "🔮 永続的な総合魔力ボーナス"
}

@Composable
fun PrestigeScreen(gameViewModel: GameViewModel, paddingValues: PaddingValues) {
    val gameState by gameViewModel.gameState.collectAsState()

    LazyColumn(modifier = Modifier.padding(paddingValues)) {
        item {
            val maxStudents = (gameState.facilities[com.takanakonbu.academiamagica.model.FacilityType.GREAT_HALL]?.level ?: 0) * 10
            OverallPowerCard(
                totalMagicalPower = gameState.totalMagicalPower,
                currentMana = gameState.mana,
                manaPerSecond = gameState.manaPerSecond,
                currentGold = gameState.gold,
                goldPerSecond = gameState.goldPerSecond,
                totalStudents = gameState.students.totalStudents,
                maxStudents = maxStudents,
                philosophersStones = gameState.philosophersStones
            )
            Spacer(modifier = Modifier.height(16.dp))
            ActionButtons(gameViewModel = gameViewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- 周回カテゴリ ---
        item { Text("✨ 超越リセット", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(4.dp)) }
        item {
            val stoneBoost = 1.0 + (gameState.prestigeSkills[PrestigeSkillType.STONE_BOOST]?.level?.toDouble()?.times(0.05) ?: 0.0)
            val ancientMagicBonus = 1.0 + (gameState.departments[com.takanakonbu.academiamagica.model.DepartmentType.ANCIENT_MAGIC]?.level?.toDouble()?.times(0.1) ?: 0.0)
            val newStones = if (gameState.totalMagicalPower <= BigDecimal.ONE) 0 else (Math.log10(gameState.totalMagicalPower.toDouble()) * ancientMagicBonus * stoneBoost).toLong()
            UpgradeItemCard(
                name = "💫 周回リセット",
                level = gameState.philosophersStones.toInt(),
                effect = "世界をリセットし、総合魔力に応じた賢者の石を獲得する。(${formatInflationNumber(gameState.totalMagicalPower)} -> $newStones 石)",
                costText = "実行",
                isEnabled = newStones > 0,
                onUpgrade = { gameViewModel.prestige() },
                showLevel = false
            )
        }

        // --- 超越スキルカテゴリ ---
        item { Spacer(Modifier.height(16.dp)); Text("💎 超越スキル", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(4.dp)) }
        items(gameState.prestigeSkills.entries.toList()) { (type, state) ->
            val cost = (state.level + 1).toLong()
            val maxLevel = when (type) {
                PrestigeSkillType.OFFLINE_TIME_EXTENSION -> 18
                PrestigeSkillType.RESEARCH_DISCOUNT, PrestigeSkillType.FACILITY_DISCOUNT -> 90
                else -> null
            }

            val effectText = when(type) {
                PrestigeSkillType.MANA_BOOST -> "マナの生産量がレベル毎に+30%されます。"
                PrestigeSkillType.GOLD_BOOST -> "ゴールドの生産量がレベル毎に+30%されます。"
                PrestigeSkillType.RESEARCH_DISCOUNT -> "学科の研究コストがレベル毎に-1%されます。(最大90%)"
                PrestigeSkillType.FACILITY_DISCOUNT -> "施設の改築コストがレベル毎に-1%されます。(最大90%)"
                PrestigeSkillType.STONE_BOOST -> "周回時の賢者の石獲得量がレベル毎に+5%されます。"
                PrestigeSkillType.OFFLINE_TIME_EXTENSION -> {
                    val offlineTimeExtensionLevel = state.level
                    val maxOfflineMinutes = 60 + offlineTimeExtensionLevel * 10
                    "放置可能な時間がレベル毎に+10分されます。(現在: ${maxOfflineMinutes}分)"
                }
                PrestigeSkillType.MAGICAL_POWER_BOOST -> "永続的に総合魔力がレベル毎に+0.5%されます。"
            }
            UpgradeItemCard(
                name = type.toJapanese(),
                level = state.level,
                maxLevel = maxLevel,
                effect = effectText,
                costText = if (maxLevel != null && state.level >= maxLevel) "最大レベル" else "強化 (賢者の石: $cost)",
                isEnabled = gameState.philosophersStones >= cost && (maxLevel == null || state.level < maxLevel),
                onUpgrade = { gameViewModel.upgradePrestigeSkill(type) }
            )
        }
    }
}
