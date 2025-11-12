package com.takanakonbu.academiamagica.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object School : Screen("school", "学校", Icons.Filled.AccountBalance)
    object Facility : Screen("facility", "施設", Icons.Filled.Apartment)
    object Department : Screen("department", "学科", Icons.Filled.Book)
    object Prestige : Screen("prestige", "超越", Icons.Filled.WorkspacePremium)
}

val navigationItems = listOf(
    Screen.School,
    Screen.Facility,
    Screen.Department,
    Screen.Prestige
)
