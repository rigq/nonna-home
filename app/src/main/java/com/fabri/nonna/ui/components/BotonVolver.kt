package com.fabri.nonna.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabri.nonna.ui.theme.Colores

@Composable
fun BotonVolver(onClick: () -> Unit, texto: String = "VOLVER AL INICIO") {
    BotonGrande(
        emoji = "", titulo = "◀  $texto", subtitulo = null,
        color = Colores.volver, modifier = Modifier.fillMaxWidth().height(120.dp), onClick = onClick
    )
}
