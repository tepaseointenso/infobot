package com.seotepa.infobotApp

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.seotepa.infobotApp.navigation.AppScreens
import com.seotepa.infobotApp.ui.theme.SdkTheme
import com.seotepa.infobotApp.ui.theme.SofiaSans


data class Boton(val text: String, val image:Int, val route:String)
data class Evaluacion(val estrellas: Int)

val botones = listOf(
    Boton("Profesores", image = R.drawable.civil, route = AppScreens.AcademicosScreen.route),
    Boton("Carreras", image = R.drawable.datos, route = AppScreens.CarrerasScreen.route),
    Boton("Diplomados", image = R.drawable.informatica, route = AppScreens.DiplomadosScreen.route)
)


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantUI(navController: NavController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),

                title = {
                    Text(
                        "INFOBOT PUCV",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.displayLarge,
                        fontFamily = SofiaSans
                    )
                },
            )
        },
        content = {
            Column {
                BotonesContainer(navController, modifier = Modifier.padding(it))

                // Agrega el Spacer y el botón de evaluación dentro del contenido
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.GraphicEq,
                        contentDescription = "Asistente de Voz",
                        modifier = Modifier.size(size = 120.dp)
                    )
                    Button(
                        onClick = { BotFunctions.askQuestion()}
                    ){
                        Text(
                            textAlign = TextAlign.Center,
                            text = "Pregúntame cualquier cosa!",
                            style = MaterialTheme.typography.headlineLarge,
                            fontFamily = SofiaSans
                        )

                    }


                }
            }
        }
    )
}
@Composable
fun TarjetaBotones(boton: Boton, navController: NavController) {
    Card(
        modifier = Modifier
            .clickable {
                navController.navigate(route = boton.route)
            }
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            // Imagen de fondo
            Image(
                painter = painterResource(id = boton.image),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                alpha = 0.4f,
                modifier = Modifier
                    .background(Color.Black)
                    .fillMaxSize()
            )

            // Texto centrado abajo
            Text(
                text = boton.text,
                modifier = Modifier // Centra el texto en la parte inferior
                    .padding(16.dp) // Margen interno
                    .align(Alignment.Center),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.inversePrimary,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = SofiaSans
            )
        }
    }
}

@Composable
fun BotonesContainer(navController: NavController, modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            items(botones.chunked(3)) { rowOfBotones ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowOfBotones.forEach { boton ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            TarjetaBotones(boton = boton, navController)
                        }
                    }
                }
            }
        }

        // Botón de evaluación como botón flotante en la esquina inferior derecha
        FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.primary,
            onClick = {
                // Navegar a la página de evaluación al hacer clic en el botón "EVALUAR"
                navController.navigate(route = "evaluacion")
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Estrella",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Evalúame!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = SofiaSans
                )
            }
        }
    }
}






@Composable
@Preview(
    widthDp = 1280,
    heightDp = 720,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE)
fun AssistantUIPreview() {
    SdkTheme {
        val navController = rememberNavController()
        AssistantUI(navController)
    }
}

