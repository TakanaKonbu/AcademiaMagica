package com.takanakonbu.academiamagica.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takanakonbu.academiamagica.model.DepartmentType
import com.takanakonbu.academiamagica.model.FacilityType
import com.takanakonbu.academiamagica.ui.theme.AmethystPurple
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

private fun formatInflationNumber(value: BigDecimal): String {
    if (value.compareTo(BigDecimal.ZERO) == 0) return "0.00"
    val threshold = BigDecimal("1E6")
    return if (value >= threshold) {
        DecimalFormat("0.00E0").format(value)
    } else {
        DecimalFormat("#,##0.00").format(value)
    }
}

// UI表示用の日本語変換
private fun DepartmentType.toJapanese(): String = when (this) {
    DepartmentType.ATTACK_MAGIC -> "🔥攻撃魔法"
    DepartmentType.BOTANY -> "🌿植物学"
    DepartmentType.DEFENSE_MAGIC -> "🛡️防衛魔法"
    DepartmentType.ANCIENT_MAGIC -> "📖古代魔術"
}

private fun FacilityType.toJapanese(): String = when (this) {
    FacilityType.GREAT_HALL -> "🏰大講堂"
    FacilityType.RESEARCH_WING -> "💡研究棟"
    FacilityType.DIMENSIONAL_LIBRARY -> "📚️次元図書館"
}

@Composable
fun OverallPowerCard(
    totalMagicalPower: BigDecimal,
    currentMana: BigDecimal,
    manaPerSecond: BigDecimal,
    currentGold: BigDecimal,
    goldPerSecond: BigDecimal,
    totalStudents: Int,
    maxStudents: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "総合魔力", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = formatInflationNumber(totalMagicalPower), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(24.dp))

            // リソース表示
            listOf("マナ" to (currentMana to manaPerSecond), "ゴールド" to (currentGold to goldPerSecond)).forEach { (label, values) ->
                val (current, perSecond) = values
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = label, fontFamily = FontFamily.Serif, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Text(text = formatInflationNumber(current), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(text = "(+${formatInflationNumber(perSecond)}/秒)", modifier = Modifier.fillMaxWidth(), fontFamily = FontFamily.Serif, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 生徒数
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "生徒数", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                Text(text = "$totalStudents / $maxStudents 人", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun UpgradeItemCard(
    name: String,
    level: Int,
    maxLevel: Int? = null,
    effect: String,
    costText: String,
    isEnabled: Boolean,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            val levelText = if (maxLevel != null) "レベル: $level / $maxLevel" else "レベル: $level"
            Text(text = levelText, fontFamily = FontFamily.Serif, fontSize = 16.sp)

            if (maxLevel != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val progress = if (maxLevel > 0) level.toFloat() / maxLevel.toFloat() else 0f
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = AmethystPurple, trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "効果: $effect", fontFamily = FontFamily.Serif, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onUpgrade, enabled = isEnabled, modifier = Modifier.align(Alignment.End), shape = RoundedCornerShape(2.dp)) {
                Text(text = costText, fontFamily = FontFamily.Serif)
            }
        }
    }
}

@Composable
fun GameScreen(gameViewModel: GameViewModel, paddingValues: PaddingValues) {
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
                maxStudents = maxStudents
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- 生徒カテゴリ ---
        item { Text("運営", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(4.dp)) }
        item {
            val maxStudents = (gameState.facilities[FacilityType.GREAT_HALL]?.level ?: 0) * 10
            val cost = BigDecimal("1.2").pow(gameState.students.totalStudents).multiply(BigDecimal(10))
            UpgradeItemCard(
                name = "生徒募集",
                level = gameState.students.totalStudents,
                maxLevel = maxStudents,
                effect = "マナとゴールドの基本生産量を増加させる",
                costText = "募集 (マナ: ${formatInflationNumber(cost)})",
                isEnabled = gameState.mana >= cost && gameState.students.totalStudents < maxStudents,
                onUpgrade = { gameViewModel.recruitStudent() }
            )
        }

        // --- 施設カテゴリ ---
        item { Spacer(Modifier.height(16.dp)); Text("施設", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(4.dp)) }
        items(gameState.facilities.entries.toList()) { (type, state) ->
            val cost = BigDecimal("2.0").pow(state.level).multiply(BigDecimal(100))
            val effectText = when(type) {
                FacilityType.GREAT_HALL -> "生徒の最大定員を+10人増加させる"
                FacilityType.RESEARCH_WING -> "全ての学科の最大レベルを+1引き上げる"
                FacilityType.DIMENSIONAL_LIBRARY -> "学科の研究コストを1%ずつ削減する"
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

        // --- 学科カテゴリ ---
        item { Spacer(Modifier.height(16.dp)); Text("学科", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(4.dp)) }
        items(gameState.departments.entries.toList()) { (type, state) ->
            val libraryDiscount = BigDecimal.ONE - (gameState.facilities[FacilityType.DIMENSIONAL_LIBRARY]?.level?.toBigDecimal()?.multiply(BigDecimal("0.01")) ?: BigDecimal.ZERO)
            val cost = BigDecimal("1.5").pow(state.level).multiply(BigDecimal(10)).multiply(libraryDiscount).setScale(0, RoundingMode.CEILING)
            val maxLevel = gameState.facilities[FacilityType.RESEARCH_WING]?.level ?: 0
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
