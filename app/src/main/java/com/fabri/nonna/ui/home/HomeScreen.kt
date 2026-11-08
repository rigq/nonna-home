package com.fabri.nonna.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fabri.nonna.acciones.Accion
import com.fabri.nonna.navegacion.Controlador
import com.fabri.nonna.ui.components.BotonGrande
import com.fabri.nonna.ui.theme.Colores
import com.fabri.nonna.ui.theme.Medidas

@Composable
fun HomeScreen(
    controlador: Controlador,
    temperatura: String?,
    onToqueReloj: () -> Unit,
    onFamilia: () -> Unit,
    onDia: () -> Unit
) {
    val sep = Arrangement.spacedBy(Medidas.separacion)
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion), verticalArrangement = sep) {
        Cabecera(temperatura, onToqueReloj)
        Row(Modifier.weight(1f), horizontalArrangement = sep) {
            BotonGrande("👨‍👩‍👧", "FAMILIA", "Llamar y mensajes", Colores.familia, Modifier.weight(1f).fillMaxHeight()) {
                if (controlador.permitir("Familia")) onFamilia()
            }
            BotonGrande("📷", "CÁMARA", "Hacer fotos", Colores.camara, Modifier.weight(1f).fillMaxHeight()) {
                controlador.pulsar(Accion.Camara, "Abriendo la cámara")
            }
        }
        Row(Modifier.weight(1f), horizontalArrangement = sep) {
            BotonGrande("▶️", "YOUTUBE", "Ver vídeos", Colores.youtube, Modifier.weight(1f).fillMaxHeight()) {
                controlador.pulsar(Accion.YouTube, "Abriendo YouTube")
            }
            BotonGrande("💬", "PREGUNTAR", "Hablar con ChatGPT", Colores.chatgpt, Modifier.weight(1f).fillMaxHeight()) {
                controlador.pulsar(Accion.ChatGptVoz, "Abriendo ChatGPT")
            }
        }
        // onDia decide si navega (permitir) o abre la app DIA (pulsar): ver AppAbuela.
        BotonGrande("🛒", "MI TARJETA DIA", "Enseñar en la caja", Colores.dia, Modifier.weight(0.8f).fillMaxWidth()) { onDia() }
    }
}
