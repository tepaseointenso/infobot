package com.seotepa.infobotApp

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.seotepa.infobotApp.ui.theme.SofiaSans

val carreras = listaCarreras


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardList(navController: NavController) {

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
                        "CARRERAS",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.displayLarge,
                        fontFamily = SofiaSans
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("principal") }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            tint = Color.White,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Localized description"
                        )
                    }
                },
            )
        },
        content = {
            CarrerasContent(navController, modifier = Modifier.padding(it))
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarreraCard(navController: NavController, carrera: CarreraInformatica) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseOnSurface
        ),
        onClick = { val id = carrera.id
            println("PARAMT, $id")
            // Navegar a VistaDetalleScreen y pasar el 'id' como parámetro
            navController.navigate("vistadetalle/carreras/$id")
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            Row{
                // Imagen a la izquierda
                Image(
                    painter = painterResource(id = carrera.image),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .width(250.dp) // Ajusta el ancho de la imagen según sea necesario
                        .fillMaxHeight()
                )

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(0.3f)
                ) {
                    // Texto a la derecha
                    Text(
                        text = carrera.nombre,
                        modifier = Modifier
                            .padding(bottom = 8.dp) // Margen interno
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = SofiaSans
                    )

                    // Primera fila
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        // Columna para "Grado"
                        if (carrera.grado.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                            ) {
                                Text(
                                    text = "Grado:",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SofiaSans
                                )
                                Text(
                                    text = carrera.grado,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = SofiaSans,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }

                        // Columna para "Duración"
                        if (carrera.duracionSemestres > 0) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                            ) {
                                Text(
                                    text = "Duración:",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SofiaSans
                                )
                                Text(
                                    text = "${carrera.duracionSemestres.toString()} semestres",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = SofiaSans
                                )
                            }
                        }

                        // Columna para "Título"
                        if (carrera.titulo.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = "Título:",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SofiaSans
                                )
                                Text(
                                    text = carrera.titulo,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = SofiaSans
                                )
                            }
                        }
                    }

                    // Segunda fila
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        // Columna para "Ponderación"
                        if (carrera.ponderacion.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                            ) {
                                Text(
                                    text = "Ponderación:",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SofiaSans
                                )
                                carrera.ponderacion.forEach { (key, value) ->
                                    Text(
                                        text = "$key: $value%",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = SofiaSans
                                    )
                                }
                            }
                        }

                        // Columna para "Puntaje de Referencia"
                        if (carrera.puntajeReferencia.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                            ) {
                                Text(
                                    text = "Puntaje de Referencia:",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SofiaSans
                                )
                                carrera.puntajeReferencia.forEach { (key, value) ->
                                    Text(
                                        text = "$key: $value",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = SofiaSans
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}






@Composable
fun CarrerasContent(navController: NavController, modifier: Modifier) {
    LazyColumn (
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxHeight()
    ) {
        items(carreras.chunked(2)) { rowOfCarreras ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowOfCarreras.forEach { carrera ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(32.dp)
                            .height(500.dp)
                    ) {
                        CarreraCard(navController = navController, carrera = carrera)
                    }
                }
            }
        }
    }
}


@Composable
fun CarrerasScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    val currentPage = sharedViewModel.currentPage.value
    println("PAGINA ACTUAL, $currentPage")
    CardList(navController)
}

@Composable
@Preview(
    widthDp = 1920,
    heightDp = 1080,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE)
fun CarrerasScreenPreview() {
    val navController = rememberNavController()
    CardList(navController)
}

