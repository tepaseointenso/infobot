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
    private val _currentId = MutableLiveData<String>()
    val currentPage: LiveData<String> get() = _currentPage
    val currentId: LiveData<String> get() = _currentId
    fun setCurrentPage(page: String) {
        _currentPage.value = page
    }
    fun setId(id: String) {
        _currentId.value = id
    }
}
