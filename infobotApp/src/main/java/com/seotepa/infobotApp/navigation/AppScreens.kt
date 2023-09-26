package com.seotepa.infobotApp.navigation

sealed class AppScreens (val route: String) {
    object PrincipalScreen: AppScreens("principal")
    object UbicacionScreen: AppScreens("ubicacion")
}
