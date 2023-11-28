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

data class Profesor(val nombre: String, val image:Int, val cargo: String, val titulos: List<String>)

val profesores = listOf<Profesor>(
    Profesor("Ricardo Soto de Giorgis", R.drawable.ricardosoto, "Director de Escuela de Ingeniería Informática", listOf("PhD in Computer Science, Universidad de Nantes, Francia, 2009",
        "Ingeniero Civil en Informática, Pontificia Universidad Católica de Valparaíso, Chile, 2003",
        "Licenciado en Ciencias de la Ingeniería, Pontificia Universidad Católica de Valparaíso, Chile, 2002")),
    Profesor("Broderick Crawford Labrín", R.drawable.broderickcrawford, "Vice Decano Facultad de Ingeniería", listOf("Doctor en Ingeniería Informática, Universidad Técnica Federico Santa María, Chile, 2011",
            "Master of Business Administration, Universidad de Chile, Chile, 2001",
            "Ingeniero Civil en Informática, Universidad Técnica Federico Santa María, Chile, 1991",
            "Licenciado en Ciencias de la Ingeniería, Universidad Técnica Federico Santa María, Chile, 1988")),
    Profesor("Iván Mercado Bermúdez", R.drawable.ivanmercado, "Jefe de docencia", listOf("Ingeniero de Ejecución en Informática, Pontificia Universidad Católica de Valparaíso, Chile, 1994")),
    Profesor("Wenceslao Palma Muñoz",  R.drawable.wenceslaopalma, "Director del diplomado de Ciberseguridad y del Diplomado en BigData y DataScience", listOf("PhD in Computer Science, Universidad de Nantes, Francia, 2010",
            "Magíster en Ciencias de la Ingeniería Informática, Universidad Técnica Federico Santa María, Chile, 2004",
            "Ingeniero de Ejecución en Informática, Pontificia Universidad Católica de Valparaíso, Chile, 1997")),
    Profesor("Guillermo Cabrera Guerrero", R.drawable.guillermocabrera, "Director de Magister en Ingeniería Informática", listOf("PhD in Engineering Science, Universidad de Auckland, Nueva Zelanda, 2017",
            "Magíster en Ingeniería Industrial, Mención Logística, Pontificia Universidad Católica de Valparaíso, Chile, 2009.",
            "Ingeniero Civil en Informática, Pontificia Universidad Católica de Valparaíso, Chile, 2005.",
            "Licenciado en Ciencias de la Ingeniería, Pontificia Universidad Católica de Valparaíso, Chile, 2004.")),
    Profesor("Nibaldo Rodríguez Agurto", R.drawable.nibaldorodriguez, "Profesor Jornada Completa", listOf("Doctor en Ciencias de la Ingeniería, Universidad de Santiago de Chile, Chile, 2001",
            "Magíster en Ingeniería Informática, Universidad de Santiago de Chile, Chile, 1998",
            "Licenciado en Matemáticas y Computación, Universidad de Santiago de Chile, Chile, 1992")),
    Profesor("Claudio Cubillos Figueroa", R.drawable.claudiocubillos, "Profesor Jornada Completa", listOf("Ph.D. in Information and Systems Engineering, Politécnico de Turín, Italia, 2005",
            "Magíster en Ingeniería Industrial mención Gestión, Pontificia Universidad Católica de Valparaíso, Chile, 2001",
            "Ingeniero Civil Industrial, Pontificia Universidad Católica de Valparaíso, Chile, 2000",
            "Licenciado en Ciencias de la Ingeniería, Pontificia Universidad Católica de Valparaíso, Chile, 1999")),
    Profesor("Pamela Hermosilla Monckton", R.drawable.pamelahermosilla, "Profesora Jornada Completa", listOf("Diplomado en Comercio Electrónico y Logística Empresarial, Universidad Técnica Federico Santa María, Chile, 2002",
            "Ingeniero Civil en Informática, Universidad Técnica Federico Santa María, Chile, 1999",
            "Licenciado en Ciencias de la Ingeniería, Universidad Técnica Federico Santa María, Chile, 1998")),
    Profesor("Leslie Pérez Cáceres", R.drawable.leslieperez, "Directora del Diplomado en Inteligencia Artificial", listOf("Doctorat en Sciences de l’Ingénieur et Technologie, Universidad Libre de Bruselas, 2017",
            "Magister en Ciencias de la Ingeniería Informática, Universidad Técnica Federico Santa María, 2011",
            "Ingeniería Civil Informática, Universidad Técnica Federico Santa María, 2009")),
    Profesor("Cristian Rusu", R.drawable.cristianrusu, "Director del Diplomado en Ingeniería Informática y Director del Diplomado en Experiencia del Consumidor y Experiencia del Usuario", listOf("Doctor of Science Engineering, Universidad Técnica de Cluj-Napoca, Rumania, 2002",
            "Ingeniero Civil en Electrónica y Telecomunicaciones, Universidad Técnica de Cluj-Napoca, Rumania, 1984")),
    Profesor("Daniela Quiñones Otey", R.drawable.danielaquinones, "Profesora Jornada Completa", listOf("Doctorado en Ingeniería Informática, Pontificia Universidad Católica de Valparaíso, Chile, 2018",
            "Magíster en Ciencias de la Ingeniería Informática, Pontificia Universidad Católica de Valparaíso, Chile, 2016",
            "Ingeniero Civil en Informática, Pontificia Universidad Católica de Valparaíso, Chile, 2014",
            "Licenciado en Ciencias de la Ingeniería, Pontificia Universidad Católica de Valparaíso, Chile, 2013")),
    Profesor("Silvana Roncagliolo De La Horra", R.drawable.silvanaroncagliolo, "Profesora Jornada Completa", listOf("Master of Science, Universidad Estatal de Oregón, Oregon, Estados Unidos, 1993",
            "Ingeniero Civil en Informática, Universidad Técnica Federico Santa María, Chile, 1989",
            "Licenciado en Ciencias de la Ingeniería, Universidad Técnica Federico Santa María, Chile, 1987")),
    Profesor("Rodrigo Alfaro Arancibia", R.drawable.rodrigoalfaro, "Profesor Jornada Completa", listOf("Doctor en Ingeniería Informática, Universidad Técnica Federico Santa María, Chile, 2012",
            "Magíster en Ingeniería Industrial, Pontificia Universidad Católica de Valparaíso, Chile, 2001",
            "Ingeniero Civil Industrial, Pontificia Universidad Católica de Valparaíso, Chile, 2000",
            "Licenciado en Ciencias de la Ingeniería, Pontificia Universidad Católica de Valparaíso, Chile, 1999")),
    Profesor("Rodolgo Villarroel Acevedo", R.drawable.rodolfovillarroel, "Profesor Jornada Completa", listOf("Doctor en Ingeniería Informática, Universidad de Castilla-La Mancha, España, 2005",
            "Magister en Ingeniería Informática, Universidad Técnica Federico Santa María, Chile, 2000",
            "Ingeniería de Ejecución en Sistemas de Información, Universidad Técnica Federico Santa María, Chile, 1983")),
    Profesor("Héctor Allende Cid", R.drawable.hectorallende, "Profesor Jornada Completa",listOf("Doctor en Ingeniería Informática, Universidad Técnica Federico Santa Maria, Chile, 2015.",
            "Magíster en Ciencias de la Ingeniería Informática, Universidad Técnica Federico Santa Maria, Chile, 2009.",
            "Ingeniero Civil Informático, Universidad Técnica Federico Santa Maria, Chile, 2009.")),
    Profesor("Ignacio Araya Zamorano", R.drawable.ignacioaraya, "Jefe de Investigación", listOf("PhD in Computer Science, Universidad de Niza, Francia, 2010.",
            "Magíster en Informática, Universidad Técnica Federico Santa María, Chile, 2007.",
            "Ingeniero Civil Informático, Universidad Técnica Federico Santa María, Chile, 2007.")),
    Profesor("Sandra Cano Mazuera", R.drawable.sandracano, "Profesora Jornada Completa", listOf("Doctora en Ciencia de la Electrónica, Universidad del Cauca, Colombia 2016",
            "Ingeniera Electrónica, Universidad Autónoma de Occidente, 2002")),
    Profesor("Aldo Migliaro Osorio", R.drawable.aldomigliaro, "Profesor Extraordinario", listOf("Ingeniero Civil Químico, Pontificia Universidad Católica de Valparaíso, Chile, 1975"))
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicosScreen(navController: NavController) {
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
        AcademicosScreen(navController)
    }
}

