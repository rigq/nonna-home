package com.fabri.nonna.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.fabri.nonna.datos.Ajustes
import com.fabri.nonna.seguridad.IntentosPin
import com.fabri.nonna.seguridad.Pin

@Composable
fun PinScreen(ajustes: Ajustes, intentos: IntentosPin, onOk: () -> Unit, onCancelar: () -> Unit) {
    var mensaje by remember { mutableStateOf<String?>(null) }
    TecladoPin("PIN de administrador", mensaje, onCompleto = { p ->
        when {
            intentos.bloqueado() -> mensaje = "Espera ${intentos.segundosRestantes()} s"
            Pin.comprobar(p, ajustes.pinSal, ajustes.pinHash) -> { intentos.acierto(); onOk() }
            else -> {
                intentos.fallo()
                mensaje = if (intentos.bloqueado()) "Demasiados fallos. Espera 30 s" else "PIN incorrecto"
            }
        }
    }, onCancelar = onCancelar)
}
