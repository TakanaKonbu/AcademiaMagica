package com.takanakonbu.academiamagica.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActionButtons(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { /* TODO */ },
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
