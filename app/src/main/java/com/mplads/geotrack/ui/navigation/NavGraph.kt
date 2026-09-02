package com.mplads.geotrack.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Camera : Screen("camera")
    object Preview : Screen("preview")
    object Gallery : Screen("gallery")
}
