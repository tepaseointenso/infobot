package com.seotepa.infobotApp

// Nombre del archivo: SharedViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow

class SharedViewModel : ViewModel() {
    val navController = MutableStateFlow<NavController?>(null)
}
