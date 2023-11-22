package com.seotepa.infobotApp

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.seotepa.infobotApp.navigation.AppScreens
import com.seotepa.infobotApp.ui.theme.SdkTheme
import com.seotepa.infobotApp.ui.theme.SofiaSans

data class Boton(val text: String, val image:Int, val route:String)

val botones = listOf(
    Boton("Profesores", image = R.drawable.civil, route = AppScreens.AcademicosScreen.route),
    Boton("Carreras", image = R.drawable.datos, route = AppScreens.CarrerasScreen.route),
    Boton("Doctorados", image = R.drawable.informatica, route = AppScreens.GaleriaScreen.route)
)

//@Composable
//fun AssistantUI(navController: NavController
//) {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//    ) {
//        // Fondo de imagen
//        Image(
//            painter = painterResource(id = R.drawable.background),
//            contentDescription = null, // Proporciona una descripción adecuada si es necesario
//            contentScale = ContentScale.FillBounds,
//            modifier = Modifier
//                .fillMaxSize()
//                .blur(50.dp)
//        )
//        Card(
//            modifier = Modifier
//                .align(Alignment.Center)
//        ) {
//            Column(
//                modifier = Modifier
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.logo_escuela),
//                    contentDescription = "Logo de la escuela",
//                    contentScale = ContentScale.Inside,
//                    modifier = Modifier
//                        .width(300.dp)
//                )
//
//                BotonesContainer()
//
////                val rows = buttonLabels.chunked(3)
////
////                rows.forEach { row ->
////                    Row(
////                        horizontalArrangement = Arrangement.Center,
////                        modifier = Modifier.fillMaxWidth(0.8f)
////                    ) {
////                        row.forEach { label ->
////                            Button(
////                                onClick = { BotFunctions.askQuestion() },
////                                modifier = Modifier
////                                    .padding(8.dp)
////                                    .fillMaxWidth()
////                                    .height(150.dp) // Adjust the height as needed
////                                    .weight(1f)
////                            ) {
////                                Text(
////                                    text = label,
////                                    style = MaterialTheme.typography.bodyMedium,
////                                    color = Color.White
////                                )
////                            }
////                        }
////                    }
////                }
//            }
//
//        }
//        Button(
//            onClick = { BotFunctions.askQuestion() },
//            modifier = Modifier
//                .align(Alignment.BottomStart)  // Posiciona el botón en la esquina inferior izquierda
//                .height(200.dp)  // Establece la altura del botón
//                .width(400.dp)
//                .padding(16.dp)
//        ) {
//            Text(
//                text = "Pregunta ahora",
//                style = MaterialTheme.typography.bodyMedium,
//                color = Color.White
//            )
//        }
//    }
//}

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center, // Centra verticalmente los elementos
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BotonesContainer(navController)
//            Divider(thickness = 5.dp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(vertical = 10.dp))
        }

    }
}
@Composable
fun TarjetaBotones(boton: Boton, navController: NavController) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .clickable(onClick = { navController.navigate(route = boton.route) })
            .width(500.dp) // Establece el ancho deseado para hacer la tarjeta cuadrada
            .height(250.dp) // Establece la altura deseada para hacer la tarjeta cuadrada
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
            )

            // Texto centrado abajo
            Text(
                text = boton.text,
                modifier = Modifier // Centra el texto en la parte inferior
                    .align(Alignment.Center),
                fontFamily = SofiaSans,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp, fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.inversePrimary,
            )
        }
    }
}

@Composable
fun BotonesContainer(navController: NavController){
    Column(modifier = Modifier.padding(vertical = 26.dp)) {
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ){
            botones.forEach() {
                Column {
                    TarjetaBotones(boton = it, navController)
                }
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

