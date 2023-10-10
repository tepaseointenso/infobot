package com.seotepa.infobotApp

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.seotepa.infobotApp.ui.theme.SdkTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicosScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Academicos Escuela Ingenieria Informatica PUCV")
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back Arrow",
                        modifier = Modifier.clickable {
                            navController.popBackStack()
                        }
                    )
                }
            )
        }
    ) {
        Column {
            BodyContent(navController = navController)
        }
    }
}


@Composable
fun BodyContent(navController: NavController){
    val webViewState = rememberWebViewState(url = "https://www.inf.ucv.cl/category/academicos/")

    WebView(
        state = webViewState,
        onCreated = { webView ->
            webView.settings.javaScriptEnabled = true
        },
        modifier = Modifier.fillMaxSize() // Ocupar
    )
}

@Preview(widthDp = 1280)
@Composable
fun BodyContentPreview() {
    SdkTheme {
        val navController = rememberNavController()
        BodyContent(navController)
    }
}

