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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.seotepa.infobotApp.ui.theme.SdkTheme
import com.seotepa.infobotApp.ui.theme.SofiaSans



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicosScreen(navController: NavController, sharedViewModel: SharedViewModel) {
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
                        "ACADÉMICOS PUCV",
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
            BodyContent(navController, modifier = Modifier.padding(it))
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
fun BodyContent(navController: NavController, modifier: Modifier) {
    LazyColumn (
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
    ){
        items(profesores.chunked(2)) { rowOfProfes ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) ,// Ajusta la altura automáticamente si hay solo una fila
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowOfProfes.forEach { profesor ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .weight(1f)
                            .padding(18.dp)
                    ) {
                        CardProfesor(profesor = profesor)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardProfesor(profesor: Profesor) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseOnSurface
        ),
        onClick = { BotFunctions.speak("${profesor.nombre} es ${profesor.cargo}",showAnimationOnly = false)},
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            Row {
                // Imagen a la izquierda
                Image(
                    painter = painterResource(id = profesor.image),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .width(200.dp) // Ajusta el ancho de la imagen según sea necesario
                        .fillMaxHeight()
                )

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f)
                ) {
                    // Texto a la derecha
                    Text(
                        text = profesor.nombre,
                        modifier = Modifier
                            .padding(bottom = 8.dp) // Margen interno
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = SofiaSans
                    )

                    // LazyColumn
                    LazyColumn {
                        items(profesor.titulos) { titulo ->
                            Text(
                                text = titulo,
                                fontSize = 20.sp,
                                fontFamily = SofiaSans,
                                modifier = Modifier
                                    .padding(8.dp) // Margen interno
                            )
                        }
                    }
                }
            }
        }
    }
}





@Preview(widthDp = 1280)
@Composable
fun BodyContentPreview() {
    SdkTheme {
        val navController = rememberNavController()
        val sharedViewModel = SharedViewModel()
        AcademicosScreen(navController, sharedViewModel)
    }
}

