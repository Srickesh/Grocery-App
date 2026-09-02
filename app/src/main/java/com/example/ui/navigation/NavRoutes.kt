package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Explore")
    object Categories : Screen("categories", "Categories")
    object Cart : Screen("cart", "Cart")
    object Orders : Screen("orders", "Orders")
    object Profile : Screen("profile", "Account")
    object Checkout : Screen("checkout", "Checkout")
    object Tracking : Screen("tracking", "Live Tracking")
    object Auth : Screen("auth", "Sign In")
    object Admin : Screen("admin", "Admin Portal")
}
