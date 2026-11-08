package com.fabri.nonna.ui.error

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.fabri.nonna.ui.components.BotonVolver
import com.fabri.nonna.ui.theme.Medidas

@Composable
fun ErrorScreen(nombreAyuda: String, onVolver: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Text("😕", fontSize = 96.sp)
        Text("No se ha podido abrir.", fontSize = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("Pídele ayuda a $nombreAyuda.", fontSize = 32.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        BotonVolver(onVolver)
    }
}
