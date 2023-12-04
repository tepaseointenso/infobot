package com.seotepa.infobotApp

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.seotepa.infobotApp.ui.theme.SofiaSans

// En el composable donde recibes los parámetros


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaDetalleScreen(navController: NavController, id: String?, sharedViewModel: SharedViewModel) {
    val tipo = sharedViewModel.currentPage.value
    var diplomadoSeleccionado: Diplomado? = null
    var carreraSeleccionada: CarreraInformatica? = null
    if (tipo?.contains("diplomados") == true) {
        diplomadoSeleccionado = listaDiplomados.find { it.id == id }
        carreraSeleccionada = null
    } else {
        diplomadoSeleccionado = null
        carreraSeleccionada = listaCarreras.find { it.id == id }
    }
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
                        "VISTA DETALLADA",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.displayLarge,
                        fontFamily = SofiaSans
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (tipo != null) {
                            if (tipo.contains("diplomados")) navController.navigate("diplomados")
                            else navController.navigate("carreras")
                         }}) {
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
            var idSeleccion: String = ""
            if (carreraSeleccionada != null) {
                idSeleccion = carreraSeleccionada.id
                CarreraDetailView(carreraSeleccionada)
            }
            else if (diplomadoSeleccionado != null) {
                idSeleccion = diplomadoSeleccionado.id
                DiplomadoDetailView(navController, modifier = Modifier.padding(it), diplomado = diplomadoSeleccionado)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ){
                // Botón + INFO en la esquina superior derecha
                Button(
                    onClick = { BotFunctions.preguntarDetalles()},
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        textAlign = TextAlign.Center,
                        text = "+ INFO",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = SofiaSans
                    )
                }
            }
        }
,
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
fun DiplomadoDetailView(navController: NavController, modifier: Modifier, diplomado: Diplomado) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sección de la imagen del diplomado
        Image(
            painter = painterResource(id = diplomado.image),
            contentDescription = "Diplomado Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(shape = RoundedCornerShape(8.dp))
        )

        // Sección de detalles generales
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = diplomado.title,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = SofiaSans
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Duración: ${diplomado.duracion}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Fechas: ${diplomado.fechas.first} - ${diplomado.fechas.second}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Horario: ${diplomado.horario.first}, ${diplomado.horario.second}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Arancel: ${diplomado.arancel}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Modalidad: ${diplomado.modalidad}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Lugar: ${diplomado.lugar}",
            style = MaterialTheme.typography.bodyMedium
        )

        // Otras secciones (objetivos, módulos, profesores, etc.)
        // Puedes seguir el mismo patrón para cada sección de información

        // Sección de módulos
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Módulos:",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = SofiaSans
        )
        diplomado.modulos.forEach { modulo ->
            Text(
                text = "- $modulo",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Sección de profesores
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Profesores:",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = SofiaSans
        )
        diplomado.profesores.forEach { profesor ->
            Text(
                text = "- $profesor",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Otras secciones...

        // Sección de descuentos
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Descuentos:",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = SofiaSans
        )
        diplomado.descuentos?.forEach { descuento ->
            Text(
                text = "- $descuento",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Otras secciones...

    }
}

@Composable
fun CarreraDetailView(carrera: CarreraInformatica) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sección de la imagen de la carrera
        Image(
            painter = painterResource(id = carrera.image),
            contentDescription = "Carrera Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(shape = RoundedCornerShape(8.dp))
        )

        // Sección de detalles generales
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = carrera.nombre,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = SofiaSans
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Grado: ${carrera.grado}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Título: ${carrera.titulo}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Duración (semestres): ${carrera.duracionSemestres}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Código: ${carrera.codigo}",
            style = MaterialTheme.typography.bodyMedium
        )

        // Otras secciones (malla, ponderación, puntajeReferencia, resenaCarrera, campoOcupacional, perfilEgreso, etc.)
        // Puedes seguir el mismo patrón para cada sección de información

        // Sección de malla
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Malla: ${carrera.malla}",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = SofiaSans
        )

        // Sección de ponderación
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ponderación:",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = SofiaSans
        )
        carrera.ponderacion.forEach { (materia, ponderacion) ->
            Text(
                text = "- $materia: $ponderacion",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Sección de puntaje de referencia
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Puntaje de Referencia:",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = SofiaSans
        )
        carrera.puntajeReferencia.forEach { (tipo, puntaje) ->
            Text(
                text = "- $tipo: $puntaje",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Otras secciones...

        // Sección de resenaCarrera
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Reseña de Carrera: ${carrera.resenaCarrera}",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = SofiaSans
        )

        // Sección de campoOcupacional
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Campo Ocupacional: ${carrera.campoOcupacional}",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = SofiaSans
        )

        // Sección de perfilEgreso
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Perfil de Egreso:",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = SofiaSans
        )
        carrera.perfilEgreso.forEach { perfil ->
            Text(
                text = "- $perfil",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Otras secciones...

    }
}

