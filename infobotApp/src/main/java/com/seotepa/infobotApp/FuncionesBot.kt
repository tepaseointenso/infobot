package com.seotepa.infobotApp

import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.TtsRequest.Companion.create

// FuncionesBot.kt

// Aquí puedes definir tus funciones y variables globales
// en caso de que necesites alguna.
// ...

// Objeto compañero para organizar funciones
lateinit var robot: Robot
object BotFunctions {
    val robot = Robot.getInstance()


    // Puedes definir tus funciones aquí
    fun speak(text: String, showAnimationOnly: Boolean = false) {
        val ttsRequest = create(text, language = TtsRequest.Language.ES_ES, showAnimationOnly = showAnimationOnly)
        robot.speak(ttsRequest)
    }

     fun getNickName() {
        speak("temi's nick name: ${robot.getNickName()}")
    }

    fun followMe() {
        robot.beWithMe()
    }

    fun hideTopBar() {
        robot.hideTopBar()
    }

    fun showTopBar() {
        robot.showTopBar()
    }

    fun askQuestion() {
        robot.askQuestion("¿En que puedo ayudarte?")
    }

    // Otras funciones pueden ir aquí

}

// También puedes tener funciones y variables fuera del objeto compañero si es necesario
// ...

// Ejemplo de cómo usar la función desde otro archivo
// val texto = "Hola, mundo!"
// BotFunctions.speak(texto)
