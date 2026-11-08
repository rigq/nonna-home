package com.fabri.nonna.feedback

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class Voz(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var lista = false
    var activada = true

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.forLanguageTag("es-ES")
            lista = true
        }
    }

    fun decir(texto: String) {
        if (activada && lista) tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "nonna")
    }

    fun cerrar() = tts.shutdown()
}
