package com.fabri.nonna.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun TemaAbuela(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Colores.fondo, surface = Colores.fondo,
            onBackground = Colores.texto, onSurface = Colores.texto
        ),
        content = content
    )
}
