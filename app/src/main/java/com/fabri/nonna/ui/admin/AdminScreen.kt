package com.fabri.nonna.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdminScreen(
    onFamiliares: () -> Unit,
    onAjustes: () -> Unit,
    onDiagnostico: () -> Unit,
    onSalirLauncher: () -> Unit,
    onVolver: () -> Unit
) = PantallaAdmin("Administración", onVolver) { relleno ->
    val flecha = @Composable { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) }
    Column(Modifier.padding(relleno)) {
        FilaAdmin("Familiares", "Contactos, fotos y orden", Icons.Default.Person, final = flecha, onClick = onFamiliares)
        FilaAdmin("Ajustes", "Tarjeta DIA, ciudad, voz y PIN", Icons.Default.Settings, final = flecha, onClick = onAjustes)
        FilaAdmin("Comprobar botones", "Permisos y prueba de cada acción", Icons.Default.CheckCircle, final = flecha, onClick = onDiagnostico)
        HorizontalDivider()
        FilaAdmin("Salir al launcher normal", "Elegir otra pantalla de inicio", Icons.Default.Home, onClick = onSalirLauncher)
    }
}
