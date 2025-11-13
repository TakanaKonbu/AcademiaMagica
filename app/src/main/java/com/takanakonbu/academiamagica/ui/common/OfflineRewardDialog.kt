package com.takanakonbu.academiamagica.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun OfflineRewardDialog(
    minutes: Long,
    manaGained: String,
    goldGained: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "おかえりなさい！")
        },
        text = {
            Text(text = "${minutes}分の放置により、以下の報酬を獲得しました。\n✨ マナ: $manaGained\n💰 ゴールド: $goldGained")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("広告を見て2倍")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}
