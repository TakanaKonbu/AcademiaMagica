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
import java.text.DecimalFormat

private fun formatInflationNumber(value: BigDecimal): String {
    val threshold = BigDecimal("1E6")
    return if (value >= threshold) {
        DecimalFormat("0.00E0").format(value)
    } else {
        DecimalFormat("#,##0.00").format(value)
    }
}

private fun DepartmentType.toJapanese(): String {
    return when (this) {
        DepartmentType.ATTACK_MAGIC -> "攻撃魔法"
        DepartmentType.BOTANY -> "植物学"
        DepartmentType.DEFENSE_MAGIC -> "防衛魔法"
        DepartmentType.ANCIENT_MAGIC -> "古代魔術"
    }
}

private fun FacilityType.toJapanese(): String {
    return when (this) {
        FacilityType.GREAT_HALL -> "大講堂"
        FacilityType.RESEARCH_WING -> "研究棟"
        FacilityType.DIMENSIONAL_LIBRARY -> "次元図書館"
    }
}

@Composable
fun OverallPowerCard(
    totalMagicalPower: BigDecimal,
    currentMana: BigDecimal,
    manaPerSecond: BigDecimal,
    currentGold: BigDecimal,
    goldPerSecond: BigDecimal,
    totalStudents: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 総合魔力
            Text(
                text = "総合魔力",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatInflationNumber(totalMagicalPower),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // マナ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "マナ",
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = formatInflationNumber(currentMana),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "(+${formatInflationNumber(manaPerSecond)}/秒)",
                modifier = Modifier.fillMaxWidth(),
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ゴールド
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ゴールド",
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = formatInflationNumber(currentGold),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "(+${formatInflationNumber(goldPerSecond)}/秒)",
                modifier = Modifier.fillMaxWidth(),
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 生徒数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "生徒数",
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = "$totalStudents 人",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun UpgradeItemCard(
    name: String,
    level: Int? = null,
    maxLevel: Int? = null,
    effect: String,
    manaCost: BigDecimal,
    goldCost: BigDecimal = BigDecimal.ZERO,
    currentMana: BigDecimal,
    currentGold: BigDecimal,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (level != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "レベル: $level", fontFamily = FontFamily.Serif, fontSize = 16.sp)
            }
            if (level != null && maxLevel != null) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { level.toFloat() / maxLevel },
                    modifier = Modifier.fillMaxWidth(),
                    color = AmethystPurple,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "効果: $effect", fontFamily = FontFamily.Serif, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onUpgrade,
                enabled = currentMana >= manaCost && currentGold >= goldCost,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(2.dp)
            ) {
                val costText = if (goldCost > BigDecimal.ZERO) {
                    "強化 (マナ: ${formatInflationNumber(manaCost)}, ゴールド: ${formatInflationNumber(goldCost)})"
                } else {
                    "強化 (コスト: ${formatInflationNumber(manaCost)})"
                }
                Text(text = costText, fontFamily = FontFamily.Serif)
            }
        }
    }
}

@Composable
fun GameScreen(gameViewModel: GameViewModel, paddingValues: PaddingValues) {
    val gameState by gameViewModel.gameState.collectAsState()
    val manaPerSecond = BigDecimal.ONE.add(BigDecimal(gameState.philosophersStones))
    val goldPerSecond = gameState.facilities[FacilityType.RESEARCH_WING]?.level?.let { BigDecimal(it) } ?: BigDecimal.ZERO

    LazyColumn(modifier = Modifier.padding(paddingValues)) {
        item {
            OverallPowerCard(
                totalMagicalPower = gameState.totalMagicalPower,
                currentMana = gameState.mana,
                manaPerSecond = manaPerSecond,
                currentGold = gameState.gold,
                goldPerSecond = goldPerSecond,
                totalStudents = gameState.students.totalStudents
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "運営",
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            val cost = BigDecimal(100).multiply(BigDecimal(gameState.students.totalStudents + 1))
            UpgradeItemCard(
                name = "生徒募集",
                effect = "生徒が1人増える",
                manaCost = cost,
                currentMana = gameState.mana,
                currentGold = gameState.gold,
                onUpgrade = { gameViewModel.recruitStudent() }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "学科",
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(gameState.departments.entries.toList()) { (type, state) ->
            val cost = BigDecimal(10).pow(state.level)
            UpgradeItemCard(
                name = type.toJapanese(),
                level = state.level,
                maxLevel = 100, // Dummy Data
                effect = "+${state.level + 1} マナ/秒", // Dummy Data
                manaCost = cost,
                currentMana = gameState.mana,
                currentGold = gameState.gold,
                onUpgrade = { gameViewModel.upgradeDepartment(type) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "施設",
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(gameState.facilities.entries.toList()) { (type, state) ->
            val manaCost = BigDecimal(20).pow(state.level)
            var goldCost = BigDecimal.ZERO
            if (type == FacilityType.DIMENSIONAL_LIBRARY) {
                goldCost = BigDecimal(100).pow(state.level)
            }
            UpgradeItemCard(
                name = type.toJapanese(),
                level = state.level,
                maxLevel = 50, // Dummy Data
                effect = if (type == FacilityType.RESEARCH_WING) "+${state.level + 1} ゴールド/秒" else "総合魔力ボーナス +${state.level * 5}%",
                manaCost = manaCost,
                goldCost = goldCost,
                currentMana = gameState.mana,
                currentGold = gameState.gold,
                onUpgrade = { gameViewModel.upgradeFacility(type) }
            )
        }
    }
}
