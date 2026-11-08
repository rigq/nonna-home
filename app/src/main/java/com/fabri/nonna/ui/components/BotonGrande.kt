package com.fabri.nonna.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.nonna.ui.theme.Colores
import com.fabri.nonna.ui.theme.Medidas

@Composable
fun BotonGrande(
    emoji: String,
    titulo: String,
    subtitulo: String?,
    color: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = Colores.textoSobreBoton,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = Medidas.botonMin),
        shape = RoundedCornerShape(24.dp),
        color = color,
        contentColor = contentColor,
        shadowElevation = 4.dp
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (emoji.isNotEmpty()) Text(emoji, fontSize = 44.sp)
            // Una sola línea que encoge lo justo: nunca parte una palabra ("VIDEOL-LAMAD-A").
            BasicText(
                titulo,
                style = TextStyle(color = LocalContentColor.current, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = Medidas.titulo)
            )
            if (subtitulo != null) Text(subtitulo, fontSize = Medidas.subtitulo, textAlign = TextAlign.Center)
        }
    }
}

@Preview(widthDp = 200, heightDp = 200)
@Composable
private fun VistaBoton() {
    BotonGrande("📷", "CÁMARA", "Hacer fotos", Colores.camara) {}
}
