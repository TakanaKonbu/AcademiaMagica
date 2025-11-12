package com.takanakonbu.academiamagica.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takanakonbu.academiamagica.ui.theme.AmethystPurple
import java.math.BigDecimal
import java.text.DecimalFormat

fun formatInflationNumber(value: BigDecimal): String {
    if (value.compareTo(BigDecimal.ZERO) == 0) return "0.00"
    val threshold = BigDecimal("1E6")
    return if (value >= threshold) {
        DecimalFormat("0.00E0").format(value)
    } else {
        DecimalFormat("#,##0.00").format(value)
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onUpgrade, enabled = isEnabled, shape = RoundedCornerShape(2.dp)) {
                    Text(text = costText, fontFamily = FontFamily.Serif)
                }
            }
        }
    }
}

@Composable
fun OverallPowerCard(
    totalMagicalPower: BigDecimal,
    currentMana: BigDecimal,
    manaPerSecond: BigDecimal,
    currentGold: BigDecimal,
    goldPerSecond: BigDecimal,
    totalStudents: Int,
    maxStudents: Int,
    philosophersStones: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "💠 総合魔力", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = formatInflationNumber(totalMagicalPower), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(24.dp))

            // リソース表示
            listOf(
                "♦️ マナ" to (currentMana to manaPerSecond),
                "💰 ゴールド" to (currentGold to goldPerSecond)
            ).forEach { (label, values) ->
                val (current, perSecond) = values
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = label, fontFamily = FontFamily.Serif, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Text(text = formatInflationNumber(current), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(text = "(+${formatInflationNumber(perSecond)}/秒)", modifier = Modifier.fillMaxWidth(), fontFamily = FontFamily.Serif, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 賢者の石
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💎 賢者の石",
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = "$philosophersStones 個",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "(総合魔力+${philosophersStones * 10}%)",
                modifier = Modifier.fillMaxWidth(),
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 生徒数
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "👥 生徒数", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                Text(text = "$totalStudents / $maxStudents 人", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
