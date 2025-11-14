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
import com.takanakonbu.academiamagica.model.FacilityType
import com.takanakonbu.academiamagica.ui.common.ActionButtons
import com.takanakonbu.academiamagica.ui.common.OverallPowerCard
import com.takanakonbu.academiamagica.ui.common.UpgradeItemCard
import com.takanakonbu.academiamagica.ui.common.formatInflationNumber
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel
import java.math.BigDecimal

private fun FacilityType.toJapanese(): String = when (this) {
    FacilityType.GREAT_HALL -> "🏫 大広間"
    FacilityType.RESEARCH_WING -> "🔬 研究棟"
    FacilityType.DIMENSIONAL_LIBRARY -> "📚 次元の図書館"
    FacilityType.BREEDING_HUT -> "🏡 飼育小屋"
}

@Composable
fun FacilityScreen(
    gameViewModel: GameViewModel,
    paddingValues: PaddingValues
) {
    val gameState by gameViewModel.gameState.collectAsState()

    LazyColumn(
        modifier = Modifier.padding(paddingValues)
    ) {
        item {
            OverallPowerCard(
                gameState = gameState
            )
            Spacer(modifier = Modifier.height(16.dp))
            ActionButtons(gameViewModel = gameViewModel)
            Spacer(modifier = Modifier.height(16.dp))
            Text("施設", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(4.dp))
        }

        items(gameState.facilities.entries.toList()) { (type, state) ->
            val cost = BigDecimal("2.0").pow(state.level).multiply(BigDecimal(100))
            val effectText = when(type) {
                FacilityType.GREAT_HALL -> "生徒の最大人数がレベル毎に10人増加します。"
                FacilityType.RESEARCH_WING -> "学科の最大レベルがレベル毎に5増加します。"
                FacilityType.DIMENSIONAL_LIBRARY -> "総合魔力にレベル毎に+1%のボーナスを得ます。"
                FacilityType.BREEDING_HUT -> "リワード広告のボーナスがレベル毎に+1%されます。"
            }
            UpgradeItemCard(
                name = type.toJapanese(),
                level = state.level,
                effect = effectText,
                costText = "改築 (Gold: ${formatInflationNumber(cost)})",
                isEnabled = gameState.gold >= cost,
                onUpgrade = { gameViewModel.upgradeFacility(type) }
            )
        }
    }
}
