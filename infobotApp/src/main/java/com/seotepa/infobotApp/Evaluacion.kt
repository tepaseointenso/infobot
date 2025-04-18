package com.seotepa.infobotApp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.seotepa.infobotApp.ui.theme.SofiaSans


@Composable
fun EvaluacionScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    val currentPage = sharedViewModel.currentPage.value
    println("PAGINA ACTUAL, $currentPage")
    var selectedStars by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¿Qué te ha parecido la aplicación?",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = SofiaSans
        )

        Spacer(modifier = Modifier.height(16.dp))

        RatingBar(
            rating = selectedStars.toFloat(),
            onRatingChanged = {
                selectedStars = it.toInt()
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            modifier = Modifier.height(100.dp).width(400.dp),
                    onClick = {
                // Guardar la evaluación en un archivo del dispositivo o realizar otras acciones necesarias.
                // En este ejemplo, simplemente navegamos de nuevo a la pantalla anterior.
                navController.navigateUp()
            }
        ) {
            Text(text = "Guardar Evaluación", fontSize = (40.sp))
        }
    }
}

@Composable
fun RatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (1..5).forEach { starIndex ->
            val starIcon = if (starIndex <= rating) Icons.Default.Star else Icons.Default.StarBorder

            Icon(
                imageVector = starIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        onRatingChanged(starIndex.toFloat())
                    }
            )
        }
    }
}


