package com.fabri.nonna.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.nonna.acciones.Accion
import com.fabri.nonna.acciones.WhatsAppContactos
import com.fabri.nonna.admin.Comprobacion
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.navegacion.Controlador
import com.fabri.nonna.ui.components.BotonVolver

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
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Comprobaciones", fontSize = 26.sp) }
            if (comprobaciones.isEmpty()) item { Text("Comprobando…", fontSize = 20.sp) }
            if (comprobaciones.isNotEmpty() && !WhatsAppContactos.tienePermisos(ctx)) item {
                Button(onClick = { pedir.launch(WhatsAppContactos.PERMISOS) }) { Text("Dar permisos de contactos y llamadas") }
            }
            items(comprobaciones) { c ->
                Column {
                    Text("${if (c.ok) "✅" else "❌"} ${c.nombre}", fontSize = 20.sp)
                    if (!c.ok) Text("   → ${c.ayuda}", fontSize = 16.sp)
                }
            }
            item { Text("Probar acciones", fontSize = 26.sp) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { controlador.pulsar(Accion.Camara, "Probando cámara") }) { Text("Cámara") }
                    OutlinedButton(onClick = { controlador.pulsar(Accion.YouTube, "Probando YouTube") }) { Text("YouTube") }
                    OutlinedButton(onClick = { controlador.pulsar(Accion.ChatGptVoz, "Probando ChatGPT") }) { Text("ChatGPT voz") }
                    OutlinedButton(onClick = { controlador.pulsar(Accion.AbrirWhatsApp, "Probando WhatsApp") }) { Text("WhatsApp") }
                    OutlinedButton(onClick = { controlador.pulsar(Accion.TarjetaDiaApp, "Probando DIA") }) { Text("App DIA") }
                    familiares.forEach { f ->
                        OutlinedButton(onClick = { controlador.pulsar(Accion.WhatsAppChat(f.telefono), "Probando chat") }) { Text("Chat con ${f.nombre}") }
                    }
                }
            }
        }
        BotonVolver(onVolver)
    }
}
