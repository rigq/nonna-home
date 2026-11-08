package com.fabri.nonna.ui.familia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.ui.components.BotonGrande
import com.fabri.nonna.ui.components.FotoFamiliar
import com.fabri.nonna.ui.theme.Colores
import com.fabri.nonna.ui.theme.Medidas

@Composable
fun TarjetaFamiliar(f: Familiar, nuevos: Int, onVideo: () -> Unit, onMensajes: () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = Colores.familia.copy(alpha = 0.35f), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Medidas.separacion), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FotoFamiliar(f.nombre, f.rutaFoto, 96.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(f.nombre, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    if (nuevos > 0) Text(
                        "🔴 $nuevos ${if (nuevos == 1) "nuevo" else "nuevos"}",
                        fontSize = 24.sp, color = Colores.aviso, fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonGrande("📹", "VIDEOLLAMADA", null, Colores.si, Modifier.weight(1f), contentColor = Color.White, onClick = onVideo)
                BotonGrande("🎤", "MENSAJES", null, Color(0xFF1565C0), Modifier.weight(1f), contentColor = Color.White, onClick = onMensajes)
            }
        }
    }
}
