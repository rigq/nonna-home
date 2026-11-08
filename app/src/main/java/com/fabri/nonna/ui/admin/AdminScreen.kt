package com.fabri.nonna.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fabri.nonna.ui.components.BotonGrande
import com.fabri.nonna.ui.components.BotonVolver
import com.fabri.nonna.ui.theme.Colores
import com.fabri.nonna.ui.theme.Medidas

@Composable
fun AdminScreen(
    onFamiliares: () -> Unit,
    onAjustes: () -> Unit,
    onDiagnostico: () -> Unit,
    onSalirLauncher: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion),
        verticalArrangement = Arrangement.spacedBy(Medidas.separacion)
    ) {
        Text("Administración", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        BotonGrande("👨‍👩‍👧", "Familiares", null, Colores.familia, Modifier.fillMaxWidth(), onClick = onFamiliares)
        BotonGrande("⚙️", "Ajustes", "Tarjeta DIA, ciudad, voz, PIN", Colores.camara, Modifier.fillMaxWidth(), onClick = onAjustes)
        BotonGrande("🩺", "Comprobar botones", null, Colores.chatgpt, Modifier.fillMaxWidth(), onClick = onDiagnostico)
        BotonGrande("🏠", "Salir al launcher normal", "Elegir otra pantalla de inicio", Colores.volver, Modifier.fillMaxWidth(), onClick = onSalirLauncher)
        Spacer(Modifier.weight(1f))
        BotonVolver(onVolver)
    }
}
