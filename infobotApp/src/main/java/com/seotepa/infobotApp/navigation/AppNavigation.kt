package com.seotepa.infobotApp.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seotepa.infobotApp.AcademicosScreen
import com.seotepa.infobotApp.AssistantUI
import com.seotepa.infobotApp.BotFunctions
import com.seotepa.infobotApp.CarrerasScreen
import com.seotepa.infobotApp.DiplomadosScreen
import com.seotepa.infobotApp.EvaluacionScreen
import com.seotepa.infobotApp.SharedViewModel
import com.seotepa.infobotApp.VistaDetalleScreen


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun AppNavigation(sharedViewModel: SharedViewModel){
    val navController = rememberNavController()
    sharedViewModel.navController.value = navController

    NavHost(navController = navController, startDestination = AppScreens.PrincipalScreen.route) {
        composable(route = AppScreens.PrincipalScreen.route) {
            BotFunctions.hideTopBar()
            sharedViewModel.setCurrentPage(navController.currentBackStackEntry?.destination?.route ?: "")
            AssistantUI(navController, sharedViewModel)
        }
        composable(route = AppScreens.AcademicosScreen.route){
            BotFunctions.hideTopBar()
            sharedViewModel.setCurrentPage(navController.currentBackStackEntry?.destination?.route ?: "")
            AcademicosScreen(navController,sharedViewModel)
        }
        composable(route = AppScreens.DiplomadosScreen.route){
            BotFunctions.hideTopBar()
            sharedViewModel.setCurrentPage(navController.currentBackStackEntry?.destination?.route ?: "")

            DiplomadosScreen(navController,sharedViewModel)
        }
        composable(route = AppScreens.CarrerasScreen.route){
            BotFunctions.hideTopBar()
            sharedViewModel.setCurrentPage(navController.currentBackStackEntry?.destination?.route ?: "")

            CarrerasScreen(navController,sharedViewModel)
        }
        composable(route = AppScreens.EvaluacionScreen.route){
            BotFunctions.hideTopBar()
            sharedViewModel.setCurrentPage(navController.currentBackStackEntry?.destination?.route ?: "")

            EvaluacionScreen(navController,sharedViewModel)
        }
        composable(route = "vistadetalle/diplomados/{id}") {
            val id = it.arguments?.getString("id")
            BotFunctions.hideTopBar()
            sharedViewModel.setId(id ?: "")
            sharedViewModel.setCurrentPage(navController.currentBackStackEntry?.destination?.route ?: "")
            VistaDetalleScreen(navController, id, sharedViewModel)
        }
        composable(route = "vistadetalle/carreras/{id}") {
            val id = it.arguments?.getString("id")
            BotFunctions.hideTopBar()
            sharedViewModel.setId(id ?: "")
            sharedViewModel.setCurrentPage(navController.currentBackStackEntry?.destination?.route ?: "")
            VistaDetalleScreen(navController, id, sharedViewModel)
        }
    }
}