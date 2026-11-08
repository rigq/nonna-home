package com.fabri.nonna.ui.admin

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.fabri.nonna.acciones.WhatsAppContactos
import com.fabri.nonna.mensajes.Mensajes
import com.fabri.nonna.seguridad.Pin

@Composable
fun AsistenteScreen(
    yaHayPin: Boolean,
    hayFamiliares: Boolean,
    onPinCreado: (String) -> Unit,
    onAnadirFamiliar: () -> Unit,
    onTerminar: () -> Unit
) {
    val ctx = LocalContext.current
    var paso by rememberSaveable { mutableIntStateOf(if (yaHayPin) 1 else 0) }
    var primerPin by rememberSaveable { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var contactosOk by remember { mutableStateOf(WhatsAppContactos.tienePermisos(ctx)) }
    var notifOk by remember { mutableStateOf(Mensajes.tienePermiso(ctx)) }
    val pedirContactos = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        contactosOk = WhatsAppContactos.tienePermisos(ctx)
    }
    LifecycleResumeEffect(Unit) {
        notifOk = Mensajes.tienePermiso(ctx)
        contactosOk = WhatsAppContactos.tienePermisos(ctx)
        onPauseOrDispose { }
    }

    when (paso) {
        0 -> TecladoPin(if (primerPin == null) "Crea un PIN (4-6 números)" else "Repite el PIN", mensaje, onCompleto = { p ->
            when {
                primerPin == null && !Pin.valido(p) -> mensaje = "Tienen que ser de 4 a 6 números"
                primerPin == null -> { primerPin = p; mensaje = null }
                p == primerPin -> { onPinCreado(p); paso = 1 }
                else -> { primerPin = null; mensaje = "No coinciden. Empieza otra vez" }
            }
        }, onCancelar = null)
        else -> Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Configuración inicial", fontSize = 28.sp)
            Text("1. Contactos y llamadas (para la videollamada directa) ${if (contactosOk) "✅" else ""}", fontSize = 20.sp)
            if (!contactosOk) Button(onClick = { pedirContactos.launch(WhatsAppContactos.PERMISOS) }) { Text("Dar permiso") }
            Text("2. Acceso a notificaciones (para ver los mensajes nuevos) ${if (notifOk) "✅" else ""}", fontSize = 20.sp)
            if (!notifOk) Button(onClick = {
                ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }) { Text("Abrir ajustes y activar \"Inicio Nonna - mensajes\"") }
            Text("3. Al menos un familiar ${if (hayFamiliares) "✅" else ""}", fontSize = 20.sp)
            Button(onClick = onAnadirFamiliar) { Text("Añadir familiar") }
            Text("La ciudad y la tarjeta DIA se configuran después en Ajustes.", fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            Button(onClick = onTerminar, enabled = hayFamiliares, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                Text("Terminar", fontSize = 22.sp)
            }
        }
    }
}
