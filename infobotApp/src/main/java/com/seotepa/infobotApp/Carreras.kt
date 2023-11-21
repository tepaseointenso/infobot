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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
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
import com.seotepa.infobotApp.ui.theme.SdkTheme
import com.seotepa.infobotApp.ui.theme.SofiaSans

data class Carrera(val title: String, val image:Int)

val carreras = listOf(
    Carrera("Ingeniería Civil Informática", image = R.drawable.civil),
    Carrera("Ingeniería en Ciencia de Datos", image = R.drawable.datos),
    Carrera("Ingeniería en Informática", image = R.drawable.informatica)
)

val diplomados = listOf(
    Carrera("BIG DATA Y DATA SCIENCE", image = R.drawable.civil),
    Carrera("EXPERIENCIA DEL USUARIO", image = R.drawable.datos),
    Carrera("INTELIGENCIA ARTIFICIAL", image = R.drawable.informatica),
    Carrera("EXPERIENCIA DEL CONSUMIDOR", image = R.drawable.datos),
    Carrera("CIBERSEGURIDAD", image = R.drawable.informatica)
)


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardList() {

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
                navigationIcon = {
                    IconButton(onClick = { /* do something */ }) {
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
                    Text(
                        textAlign = TextAlign.Center,
                        text = "Pregúntame cualquier cosa!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = SofiaSans
                    )
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
            CarrerasRow()
            Divider(thickness = 5.dp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(vertical = 10.dp))
            DiplomadosRow()
        }

    }
}
@Composable
fun TarjetaCarrera(carrera: Carrera) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .width(250.dp) // Establece el ancho deseado para hacer la tarjeta cuadrada
            .height(250.dp) // Establece la altura deseada para hacer la tarjeta cuadrada
    ) {
        Box {
            // Imagen de fondo
            Image(
                painter = painterResource(id = carrera.image),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                alpha = 0.4f,
                modifier = Modifier
                    .background(Color.Black)
            )

            // Texto centrado abajo
            Text(
                text = carrera.title,
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
fun CarrerasRow(){
    Column(modifier = Modifier.padding(vertical = 26.dp)) {
        Text(
            text = "Carreras disponibles",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 26.dp),
            fontFamily = SofiaSans
        )
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ){
            carreras.forEach() {
                Column {
                    TarjetaCarrera(carrera = it)
                }
            }
        }
    }
}

@Composable
fun DiplomadosRow(){
    Column() {
        Text(
            text = "Diplomados impartidos",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontFamily = SofiaSans
        )
        LazyRow (
            contentPadding = PaddingValues(36.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 36.dp),
        ){
            items(items = diplomados, itemContent = { diplomado ->
                Column {
                    TarjetaCarrera(diplomado)
                }
            })

        }
    }
}




@Composable
fun CarrerasScreen(navController: NavController) {
    CardList()
}

@Preview(wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE, widthDp = 1280)
@Composable
fun CardListPreview() {
    SdkTheme {
        CardList()
    }
}

