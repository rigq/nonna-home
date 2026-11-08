package com.fabri.nonna.ui.familia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.ui.components.FotoFamiliar
import com.fabri.nonna.ui.theme.Colores

/** Solo foto grande y nombre. Al tocarla se abre la pantalla con LLAMAR / MENSAJE. */
@Composable
fun TarjetaFamiliar(f: Familiar, nuevos: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        // Opaco: con transparencia, la sombra se transparenta y se ve un rectángulo raro dentro.
        color = Colores.familia.copy(alpha = 0.35f).compositeOver(Color.White),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                FotoFamiliar(f.nombre, f.rutaFoto, 130.dp)
                if (nuevos > 0) Box(
                    Modifier.align(Alignment.TopEnd)
                        .background(Colores.aviso, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) { Text("$nuevos", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
            }
            BasicText(
                f.nombre,
                style = TextStyle(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(minFontSize = 16.sp, maxFontSize = 30.sp)
            )
        }
    }
}
