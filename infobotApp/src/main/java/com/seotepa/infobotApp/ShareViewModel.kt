package com.seotepa.infobotApp

// Nombre del archivo: SharedViewModel.kt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow

class SharedViewModel : ViewModel() {
    val navController = MutableStateFlow<NavController?>(null)
    var parametro: String? = null
    private val _currentPage = MutableLiveData<String>()
    val currentPage: LiveData<String> get() = _currentPage
    fun setCurrentPage(page: String) {
        _currentPage.value = page
    }
}
