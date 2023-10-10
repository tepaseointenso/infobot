package com.seotepa.infobotApp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seotepa.infobotApp.AssistantUI
import com.seotepa.infobotApp.AcademicosScreen
import com.seotepa.infobotApp.CarrerasScreen
import com.seotepa.infobotApp.GaleriaScreen

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.PrincipalScreen.route){
        composable(route = AppScreens.PrincipalScreen.route) {
            AssistantUI(navController)
        }
        composable(route = AppScreens.AcademicosScreen.route){
            AcademicosScreen(navController)
        }
        composable(route = AppScreens.GaleriaScreen.route){
            GaleriaScreen(navController)
        }
        composable(route = AppScreens.CarrerasScreen.route){
            CarrerasScreen(navController)
        }
    }
}