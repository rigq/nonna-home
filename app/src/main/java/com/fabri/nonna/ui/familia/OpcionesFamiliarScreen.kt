package com.fabri.nonna.ui.familia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.ui.components.BotonGrande
import com.fabri.nonna.ui.components.BotonVolver
import com.fabri.nonna.ui.components.FotoFamiliar
import com.fabri.nonna.ui.theme.Colores
import com.fabri.nonna.ui.theme.Medidas

/** Tras tocar la tarjeta de un familiar: dos opciones grandes, LLAMAR o MENSAJE. */
@Composable
fun OpcionesFamiliarScreen(familiar: Familiar, onLlamar: () -> Unit, onMensaje: () -> Unit, onVolver: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Medidas.separacion)
    ) {
        FotoFamiliar(familiar.nombre, familiar.rutaFoto, 180.dp)
        Text(familiar.nombre, fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        BotonGrande("📹", "LLAMAR", null, Colores.si, Modifier.fillMaxWidth().weight(1f), contentColor = Color.White, onClick = onLlamar)
        BotonGrande("💬", "MENSAJE", null, Color(0xFF1565C0), Modifier.fillMaxWidth().weight(1f), contentColor = Color.White, onClick = onMensaje)
        BotonVolver(onVolver, texto = "VOLVER A FAMILIA")
    }
}
