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
import com.takanakonbu.academiamagica.ui.common.ActionButtons
import com.takanakonbu.academiamagica.ui.common.OverallPowerCard
import com.takanakonbu.academiamagica.ui.common.UpgradeItemCard
import com.takanakonbu.academiamagica.ui.common.formatInflationNumber
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel
import java.math.BigDecimal

private fun DepartmentType.toJapanese(): String = when (this) {
    DepartmentType.ATTACK_MAGIC -> "⚔️ 攻撃魔術科"
    DepartmentType.BOTANY -> "🌿 魔法植物学科"
    DepartmentType.DEFENSE_MAGIC -> "🛡️ 防衛魔術科"
    DepartmentType.ANCIENT_MAGIC -> "📜 古代魔術科"
    DepartmentType.MAGIC_CREATURE_STUDIES -> "🦄 魔法生物学科"
}

@Composable
fun DepartmentScreen(
    gameViewModel: GameViewModel,
    paddingValues: PaddingValues
) {
    val gameState by gameViewModel.gameState.collectAsState()

    LazyColumn(
        modifier = Modifier.padding(paddingValues)
    ) {
        item {
            OverallPowerCard(gameState = gameState)
            Spacer(modifier = Modifier.height(16.dp))
            ActionButtons(gameViewModel = gameViewModel)
            Spacer(modifier = Modifier.height(16.dp))
            Text("学科", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(4.dp))
        }

        items(gameState.departments.entries.toList()) { (type, state) ->
            val cost = BigDecimal("1.5").pow(state.level).multiply(BigDecimal(10))
            val effectText = when (type) {
                DepartmentType.ATTACK_MAGIC -> "攻撃魔術の基礎値がレベル毎に+10されます。"
                DepartmentType.BOTANY -> "マナとゴールドの生産量がレベル毎に+10%されます。"
                DepartmentType.DEFENSE_MAGIC -> "総合魔力にレベル毎に+5%のボーナスを得ます。"
                DepartmentType.ANCIENT_MAGIC -> "周回時の賢者の石獲得量がレベル毎に+10%されます。"
                DepartmentType.MAGIC_CREATURE_STUDIES -> "リワード広告のボーナスがレベル毎に+0.5%されます。"
            }

            UpgradeItemCard(
                name = type.toJapanese(),
                level = state.level,
                maxLevel = gameState.maxDepartmentLevel,
                effect = effectText,
                costText = "研究 (Mana: ${formatInflationNumber(cost)})",
                isEnabled = gameState.mana >= cost && state.level < gameState.maxDepartmentLevel,
                onUpgrade = { gameViewModel.upgradeDepartment(type) }
            )
        }
    }
}
