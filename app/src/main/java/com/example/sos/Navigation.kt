package com.example.sos

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sos.loginCred.LogInScreen
import com.example.sos.utils.BottomNavBar

@Composable
fun Navigation() {

    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = Screen.Login.route
    ) {

        composable(Screen.Login.route) {
            LogInScreen(rootNavController)
        }

        composable(Screen.Main.route) {
            MainScreen(
                rootNavController = rootNavController
            )
        }
    }
}
