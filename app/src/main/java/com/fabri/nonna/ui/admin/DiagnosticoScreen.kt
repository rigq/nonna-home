package com.fabri.nonna.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fabri.nonna.acciones.Accion
import com.fabri.nonna.acciones.WhatsAppContactos
import com.fabri.nonna.admin.Comprobacion
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.navegacion.Controlador

@Composable
fun DiagnosticoScreen(
    comprobaciones: List<Comprobacion>,
    controlador: Controlador,
    familiares: List<Familiar>,
    onPermisos: () -> Unit,
    onVolver: () -> Unit
) {
    val ctx = LocalContext.current
    val pedir = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { onPermisos() }
    val fallos = comprobaciones.count { !it.ok }
    val pruebas = buildList {
        add("Cámara" to Accion.Camara)
        add("YouTube" to Accion.YouTube)
        add("ChatGPT en modo voz" to Accion.ChatGptVoz)
        add("WhatsApp" to Accion.AbrirWhatsApp)
        add("App de DIA" to Accion.TarjetaDiaApp)
        familiares.forEach { add("Chat con ${it.nombre}" to Accion.WhatsAppChat(it.telefono)) }
    }

    PantallaAdmin("Comprobar botones", onVolver) { relleno ->
        LazyColumn(Modifier.padding(relleno), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (comprobaciones.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            else item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (fallos == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Box(Modifier.padding(16.dp)) {
                        Text(
                            if (fallos == 0) "Todo listo" else "$fallos ${if (fallos == 1) "cosa por revisar" else "cosas por revisar"}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            if (comprobaciones.isNotEmpty() && !WhatsAppContactos.tienePermisos(ctx)) item {
                Button(onClick = { pedir.launch(WhatsAppContactos.PERMISOS) }, modifier = margenCampo.fillMaxWidth()) {
                    Text("Dar permisos de contactos y llamadas")
                }
            }

            item { TituloSeccion("Estado") }
            items(comprobaciones) { c ->
                FilaAdmin(
                    titulo = c.nombre,
                    subtitulo = if (c.ok) null else c.ayuda,
                    icono = if (c.ok) Icons.Default.CheckCircle else Icons.Default.Warning,
                    colorIcono = if (c.ok) verdeOk else MaterialTheme.colorScheme.error
                )
            }

            item { HorizontalDivider(Modifier.padding(top = 8.dp)); TituloSeccion("Probar acciones") }
            items(pruebas) { (nombre, accion) ->
                FilaAdmin(nombre, icono = Icons.Default.PlayArrow) { controlador.pulsar(accion, "Probando $nombre") }
            }
        }
    }
}
