package com.takanakonbu.academiamagica.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.takanakonbu.academiamagica.model.DepartmentType
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(gameViewModel: GameViewModel, paddingValues: PaddingValues) {
    val gameState by gameViewModel.gameState.collectAsState()

    Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
        Text(text = "Mana: ${gameState.mana}")
        Text(text = "Gold: ${gameState.gold}")
        Text(text = "Total Magical Power: ${gameState.totalMagicalPower}")
        Text(text = "Philosopher's Stones: ${gameState.philosophersStones}")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Departments")
        gameState.departments.forEach { (type, state) ->
            Row {
                Text(text = "${type.name}: Lv ${state.level}")
                Button(onClick = { gameViewModel.upgradeDepartment(type) }) {
                    Text(text = "Level Up")
                }
            }
        }
    }
}
