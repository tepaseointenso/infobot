package com.seotepa.infobotApp.navigation

sealed class AppScreens (val route: String) {
    object PrincipalScreen: AppScreens("principal")
    object AcademicosScreen: AppScreens("academicos")
    object DiplomadosScreen: AppScreens("diplomados")
    object CarrerasScreen: AppScreens("carreras")
    object EvaluacionScreen: AppScreens("evaluacion")
    object VistaDetalleScreen: AppScreens("vistadetalle")
}
