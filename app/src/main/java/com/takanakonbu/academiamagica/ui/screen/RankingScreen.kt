package com.takanakonbu.academiamagica.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takanakonbu.academiamagica.model.RivalSchool
import com.takanakonbu.academiamagica.model.SchoolRanking
import com.takanakonbu.academiamagica.ui.common.ActionButtons
import com.takanakonbu.academiamagica.ui.common.OverallPowerCard
import com.takanakonbu.academiamagica.ui.common.formatInflationNumber
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel

@Composable
fun RankingScreen(gameViewModel: GameViewModel, paddingValues: PaddingValues) {
    val gameState by gameViewModel.gameState.collectAsState()
    val lazyListState = rememberLazyListState()

    // プレイヤーを含めたランキングリストを作成
    val playerSchool = RivalSchool(rank = 0, name = gameState.schoolName, power = gameState.totalMagicalPower)
    val fullRanking = (SchoolRanking.rivals + playerSchool).sortedByDescending { it.power }

    // プレイヤーの現在の順位と、次のランクのライバルを見つける
    val playerRank = fullRanking.indexOfFirst { it.name == gameState.schoolName } + 1
    val nextRival = if (playerRank > 1) fullRanking[playerRank - 2] else null

    // プレイヤーの順位までスクロール
    LaunchedEffect(playerRank) {
        if (playerRank > 0) {
            lazyListState.animateScrollToItem(index = playerRank - 1)
        }
    }

    LazyColumn(modifier = Modifier.padding(paddingValues), state = lazyListState) {
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
        item {
            // 現在の順位と次の目標を表示するカード
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(), // 中央揃えのためにfillMaxWidthを追加
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("現在の順位", fontFamily = FontFamily.Serif, fontSize = 20.sp)
                    Text("$playerRank 位 / 100校", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    nextRival?.let {
                        Text("次の順位: ${it.rank}位 ${it.name}", fontFamily = FontFamily.Serif)
                        Text("総合魔力: ${formatInflationNumber(it.power)}", fontFamily = FontFamily.Serif)
                        val diff = it.power - gameState.totalMagicalPower
                        Text("あと ${formatInflationNumber(diff)} でランクアップ", fontFamily = FontFamily.Serif, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        // ランキングヘッダー
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("順位", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                Text("学校名", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                Text("総合魔力", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            }
        }

        // ランキングリスト
        itemsIndexed(fullRanking) { index, school ->
            val isPlayer = school.name == gameState.schoolName
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isPlayer) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text((index + 1).toString(), modifier = Modifier.weight(1f))
                Text(school.name, modifier = Modifier.weight(3f))
                Text(formatInflationNumber(school.power), modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            }
        }
    }
}
