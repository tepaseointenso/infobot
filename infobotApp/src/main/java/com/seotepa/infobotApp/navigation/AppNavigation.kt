package com.seotepa.infobotApp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seotepa.infobotApp.AssistantUI
import com.seotepa.infobotApp.UbicacionScreen

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.PrincipalScreen.route){
        composable(route = AppScreens.PrincipalScreen.route) {
            AssistantUI(navController)
        }
        composable(route = AppScreens.UbicacionScreen.route){
            UbicacionScreen(navController)
        }
    }
}