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
    FacilityType.GREAT_HALL -> "🏰 大講堂"
    FacilityType.RESEARCH_WING -> "💡 研究棟"
    FacilityType.DIMENSIONAL_LIBRARY -> "📚 次元図書館"
}

@Composable
fun FacilityScreen(gameViewModel: GameViewModel, paddingValues: PaddingValues) {
    val gameState by gameViewModel.gameState.collectAsState()

    LazyColumn(modifier = Modifier.padding(paddingValues)) {
        item {
            OverallPowerCard(
                gameState = gameState
            )
            Spacer(modifier = Modifier.height(16.dp))
            ActionButtons(gameViewModel = gameViewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- 施設カテゴリ ---
        item { Text("🏰 施設", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(4.dp)) }
        items(gameState.facilities.entries.toList()) { (type, state) ->
            val cost = BigDecimal("2.0").pow(state.level).multiply(BigDecimal(100))
            val effectText = when(type) {
                FacilityType.GREAT_HALL -> "生徒の最大定員を+10人増加させる"
                // 研究棟の効果説明。GameStateで定義されたロジックと連動させる。
                FacilityType.RESEARCH_WING -> "全ての学科の最大レベルを+5引き上げる"
                FacilityType.DIMENSIONAL_LIBRARY -> "総合魔力に+1%の乗算ボーナス"
            }
            UpgradeItemCard(
                name = type.toJapanese(),
                level = state.level,
                effect = effectText,
                costText = "改築 (ゴールド: ${formatInflationNumber(cost)})",
                isEnabled = gameState.gold >= cost,
                onUpgrade = { gameViewModel.upgradeFacility(type) }
            )
        }
    }
}
