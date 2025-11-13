package com.takanakonbu.academiamagica.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel

@Composable
fun ActionButtons(
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("広告を見てマナ/ゴールドを獲得") },
            text = { Text("広告を視聴して、現在のマナとゴールドを2倍にしますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        gameViewModel.doubleManaAndGold()
                        showDialog = false
                    }
                ) {
                    Text("はい")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("いいえ")
                }
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.weight(1f)
        ) {
            Text("マナ/ゴールド獲得")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f)
        ) {
            Text("生産ブースト")
        }
    }
}
