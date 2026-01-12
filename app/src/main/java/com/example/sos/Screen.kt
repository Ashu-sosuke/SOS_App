package com.example.sos

sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Main : Screen("main")

    object HomeScreen : Screen("home")
    object Contacts : Screen("contacts")
    object MapScreen : Screen("map")
    object Setting : Screen("settings")
}

