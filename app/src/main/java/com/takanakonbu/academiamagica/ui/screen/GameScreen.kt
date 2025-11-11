package com.takanakonbu.academiamagica.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(gameViewModel: GameViewModel, paddingValues: PaddingValues) {
    val gameState by gameViewModel.gameState.collectAsState()

    Column(modifier = Modifier.padding(paddingValues)) {
        Text(text = "Mana: ${gameState.mana}")
        Text(text = "Gold: ${gameState.gold}")
        Text(text = "Total Magical Power: ${gameState.totalMagicalPower}")
        Text(text = "Philosopher's Stones: ${gameState.philosophersStones}")
    }
}
