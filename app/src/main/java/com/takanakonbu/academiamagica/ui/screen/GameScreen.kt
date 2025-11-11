package com.takanakonbu.academiamagica.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel
import java.math.BigDecimal
import java.text.DecimalFormat

@Composable
fun GameScreen(gameViewModel: GameViewModel, paddingValues: PaddingValues) {
    val gameState by gameViewModel.gameState.collectAsState()

    Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
        Text(text = "Mana: ${formatBigDecimal(gameState.mana)}")
        Text(text = "Gold: ${formatBigDecimal(gameState.gold)}")
        Text(text = "Total Magical Power: ${formatBigDecimal(gameState.totalMagicalPower)}")
        Text(text = "Philosopher's Stones: ${gameState.philosophersStones}")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Departments")
        gameState.departments.forEach { (type, state) ->
            val cost = BigDecimal(10).pow(state.level)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${type.name}: Lv ${state.level}")
                Button(
                    onClick = { gameViewModel.upgradeDepartment(type) },
                    enabled = gameState.mana >= cost
                ) {
                    Text(text = "Level Up (Cost: ${formatBigDecimal(cost)})")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Facilities")
        gameState.facilities.forEach { (type, state) ->
            val cost = BigDecimal(20).pow(state.level)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${type.name}: Lv ${state.level}")
                Button(
                    onClick = { gameViewModel.upgradeFacility(type) },
                    enabled = gameState.mana >= cost
                ) {
                    Text(text = "Level Up (Cost: ${formatBigDecimal(cost)})")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { gameViewModel.prestige() }) {
            Text(text = "Prestige")
        }
    }
}

private fun formatBigDecimal(value: BigDecimal): String {
    val threshold = BigDecimal("1E6") // 1,000,000
    return if (value >= threshold) {
        DecimalFormat("0.00E0").format(value)
    } else {
        DecimalFormat("#,##0").format(value)
    }
}
