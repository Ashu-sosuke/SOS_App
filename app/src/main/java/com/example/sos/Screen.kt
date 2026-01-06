package com.example.sos

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object HomeScreen : Screen("home")
    object Profile : Screen("profile")
    object Contacts : Screen("contacts")

}


