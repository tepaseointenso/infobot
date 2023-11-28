package com.seotepa.infobotApp

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.seotepa.infobotApp.ui.theme.SofiaSans

data class Diplomado(val title: String, val image:Int)

val diplomados = listOf(
    Diplomado("BIG DATA Y DATA SCIENCE", image = R.drawable.civil),
    Diplomado("EXPERIENCIA DEL USUARIO", image = R.drawable.datos),
    Diplomado("INTELIGENCIA ARTIFICIAL", image = R.drawable.informatica),
    Diplomado("EXPERIENCIA DEL CONSUMIDOR", image = R.drawable.datos),
    Diplomado("CIBERSEGURIDAD", image = R.drawable.informatica)
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoDiplomados(navController: NavController) {

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
                        "Diplomados",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.displayLarge,
                        fontFamily = SofiaSans
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            DiplomadosRow()
        }

    }
}
@Composable
fun TarjetaDiplomado(diplomado: Diplomado) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .width(250.dp) // Establece el ancho deseado para hacer la tarjeta cuadrada
            .height(250.dp) // Establece la altura deseada para hacer la tarjeta cuadrada
    ) {
        Box {
            // Imagen de fondo
            Image(
                painter = painterResource(id = diplomado.image),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                alpha = 0.4f,
                modifier = Modifier
                    .background(Color.Black)
            )

            // Texto centrado abajo
            Text(
                text = diplomado.title,
                modifier = Modifier // Centra el texto en la parte inferior
                    .padding(16.dp) // Margen interno
                    .align(Alignment.BottomCenter),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.inversePrimary,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
fun DiplomadosRow(){
    Column() {
        LazyRow (
            contentPadding = PaddingValues(36.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 36.dp),
        ){
            items(items = diplomados, itemContent = { diplomado ->
                Column {
                    TarjetaDiplomado(diplomado)
                }
            })

        }
    }
}




@Composable
fun DiplomadosScreen(navController: NavController) {
    ListadoDiplomados(navController)
}

