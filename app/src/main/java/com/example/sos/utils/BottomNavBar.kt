package com.example.sos.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sos.BottomNavRoute
import com.example.sos.R

@Composable
fun BottomNavBar(navController: NavController) {

    val currentRoute = navController
        .currentBackStackEntryAsState().value?.destination?.route

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {

        BottomNavItem(
            icon = painterResource(id = R.drawable.baseline_home_24),
            label = "Home",
            selected = currentRoute == BottomNavRoute.Home.route,
            onClick = {
                navController.navigate(BottomNavRoute.Home.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        BottomNavItem(
            icon = painterResource(id = R.drawable.baseline_location_pin_24),
            label = "Map",
            selected = currentRoute == BottomNavRoute.Map.route,
            onClick = {
                navController.navigate(BottomNavRoute.Map.route) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        BottomNavItem(
            icon = painterResource(id = R.drawable.outline_groups_2_24),
            label = "Contacts",
            selected = currentRoute == BottomNavRoute.Contacts.route,
            onClick = {
                navController.navigate(BottomNavRoute.Contacts.route) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        BottomNavItem(
            icon = painterResource(id = R.drawable.baseline_settings_24),
            label = "Settings",
            selected = currentRoute == BottomNavRoute.Settings.route,
            onClick = {
                navController.navigate(BottomNavRoute.Settings.route) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}


@Composable
fun BottomNavItem(
    icon: Painter,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val tint = if (selected) Color(0xFF3B82F6) else Color(0xFF6B7280)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = label,
            tint = tint
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = tint
        )
    }
}
