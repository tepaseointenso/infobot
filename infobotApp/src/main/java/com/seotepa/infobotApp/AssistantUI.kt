package com.seotepa.infobotApp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.seotepa.infobotApp.buttons.buttonLabels
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.seotepa.infobotApp.navigation.AppScreens
import com.seotepa.infobotApp.ui.theme.SdkTheme

@Composable
fun AssistantUI(navController: NavController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Fondo de imagen
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null, // Proporciona una descripción adecuada si es necesario
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp)
        )
        Card(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_escuela),
                    contentDescription = "Logo de la escuela",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .width(300.dp)
                )
                Text(
                    text = "Bienvenido al Asistente de la Universidad",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(8.dp)
                )

                Text(
                    text = "¿En qué puedo ayudarte?",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
                val rows = buttonLabels.chunked(3)

                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        row.forEach { label ->
                            Button(
                                onClick = {
                                    navController.navigate(route = AppScreens.UbicacionScreen.route)
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
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

