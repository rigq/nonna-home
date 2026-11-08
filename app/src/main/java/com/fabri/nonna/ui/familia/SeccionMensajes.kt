package com.fabri.nonna.ui.familia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.mensajes.MensajeNuevo
import com.fabri.nonna.ui.components.FotoFamiliar
import com.fabri.nonna.ui.theme.Colores
import com.fabri.nonna.ui.theme.Medidas

@Composable
fun SeccionMensajes(
    mensajes: List<MensajeNuevo>,
    familiarDe: (MensajeNuevo) -> Familiar?,
    onAbrir: (MensajeNuevo) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (mensajes.isEmpty()) {
            Text("No tienes mensajes nuevos", fontSize = Medidas.texto)
            return@Column
        }
        Text("📩 MENSAJES NUEVOS", fontSize = Medidas.titulo, fontWeight = FontWeight.Bold)
        mensajes.forEach { m ->
            val f = familiarDe(m)
            Surface(
                onClick = { onAbrir(m) }, shape = RoundedCornerShape(20.dp), color = Colores.mensaje,
                shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth().heightIn(min = Medidas.botonMin)
            ) {
                Row(Modifier.padding(Medidas.separacion), verticalAlignment = Alignment.CenterVertically) {
                    FotoFamiliar(f?.nombre ?: m.conversacion, f?.rutaFoto, 72.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(f?.nombre ?: m.conversacion, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(m.resumen, fontSize = Medidas.texto, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
