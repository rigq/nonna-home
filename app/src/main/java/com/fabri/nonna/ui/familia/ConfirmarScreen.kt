package com.fabri.nonna.ui.familia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.fabri.nonna.ui.components.FotoFamiliar
import com.fabri.nonna.ui.theme.Colores
import com.fabri.nonna.ui.theme.Medidas

@Composable
fun ConfirmarScreen(familiar: Familiar, onSi: () -> Unit, onNo: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        FotoFamiliar(familiar.nombre, familiar.rutaFoto, 160.dp)
        Text("¿Llamar a ${familiar.nombre}?", fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(Medidas.separacion)) {
            BotonGrande("✅", "SÍ, LLAMAR", null, Colores.si, Modifier.weight(1f).height(160.dp), contentColor = Color.White, onClick = onSi)
            BotonGrande("✖️", "NO", null, Colores.no, Modifier.weight(1f).height(160.dp), contentColor = Color.White, onClick = onNo)
        }
    }
}
