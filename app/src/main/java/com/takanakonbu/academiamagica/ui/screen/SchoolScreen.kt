package com.takanakonbu.academiamagica.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    DepartmentType.ATTACK_MAGIC -> "🔥 攻撃魔法"
    DepartmentType.BOTANY -> "🌿 植物学"
    DepartmentType.DEFENSE_MAGIC -> "🛡️ 防衛魔法"
    DepartmentType.ANCIENT_MAGIC -> "📖 古代魔術"
    DepartmentType.MAGIC_CREATURE_STUDIES -> "🐉 魔法生物学"
}

@Composable
fun SchoolScreen(gameViewModel: GameViewModel, paddingValues: PaddingValues) {
    val gameState by gameViewModel.gameState.collectAsState()
    var assignmentAmount by remember { mutableStateOf(1) }
    val options = listOf(1, 5, 10, 50, 100)
    var expanded by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.padding(paddingValues)) {
        item {
            OverallPowerCard(
                gameState = gameState
            )
            Spacer(modifier = Modifier.height(16.dp))
            ActionButtons(gameViewModel = gameViewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- 生徒募集カテゴリ ---
        item { Text("🏫 運営", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(4.dp)) }
        item {
            val maxStudents = (gameState.facilities[com.takanakonbu.academiamagica.model.FacilityType.GREAT_HALL]?.level ?: 0) * 10
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

        // --- 生徒配属カテゴリ ---
        item { Spacer(Modifier.height(16.dp)); Text("🎓 生徒配属", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)); Spacer(Modifier.height(4.dp)) }
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("未配属の生徒: ${gameState.students.unassignedStudents}人", fontFamily = FontFamily.Serif, fontSize = 16.sp)
                    Box {
                        Button(onClick = { expanded = true }) {
                            Text("$assignmentAmount")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(text = { Text(text = option.toString()) }, onClick = { assignmentAmount = option; expanded = false })
                            }
                        }
                    }
                }
            }
        }

        items(DepartmentType.values()) { department ->
            val effectText = when(department) {
                DepartmentType.ATTACK_MAGIC -> "効果: 総合魔力の基礎値 +5/人"
                DepartmentType.BOTANY -> "効果: マナ/ゴールド生産量 +5%/人"
                DepartmentType.DEFENSE_MAGIC -> "効果: 総合魔力ボーナス +1%/人"
                DepartmentType.ANCIENT_MAGIC -> "効果: 賢者の石獲得量 +1%/人"
                DepartmentType.MAGIC_CREATURE_STUDIES -> "効果: リワード広告のボーナス +0.5%/人"
            }
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = department.toJapanese(), fontFamily = FontFamily.Serif, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(onClick = { gameViewModel.unassignStudent(department, assignmentAmount) }, enabled = (gameState.students.specializedStudents[department] ?: 0) >= assignmentAmount) {
                                Text("-")
                            }
                            Text("${gameState.students.specializedStudents[department] ?: 0}", modifier = Modifier.padding(horizontal = 8.dp), fontFamily = FontFamily.Serif, fontSize = 16.sp)
                            Button(onClick = { gameViewModel.assignStudent(department, assignmentAmount) }, enabled = gameState.students.unassignedStudents >= assignmentAmount) {
                                Text("+")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = effectText, fontFamily = FontFamily.Serif, fontSize = 14.sp)
                }
            }
        }
    }
}
