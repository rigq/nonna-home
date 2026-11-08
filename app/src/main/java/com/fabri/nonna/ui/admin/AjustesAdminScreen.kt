package com.fabri.nonna.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.nonna.datos.Ajustes
import com.fabri.nonna.datos.TipoTarjeta
import com.fabri.nonna.seguridad.Pin
import com.fabri.nonna.tiempo.Ciudad
import com.fabri.nonna.tiempo.OpenMeteo
import com.fabri.nonna.tiempo.descargar
import kotlinx.coroutines.launch

@Composable
fun AjustesAdminScreen(ajustes: Ajustes, onGuardar: (Ajustes) -> Unit, onVolver: () -> Unit) {
    var a by remember(ajustes) { mutableStateOf(ajustes) }
    var busqueda by remember { mutableStateOf(ajustes.ciudad) }
    var resultados by remember { mutableStateOf<List<Ciudad>>(emptyList()) }
    var buscando by remember { mutableStateOf(false) }
    var sinResultados by remember { mutableStateOf(false) }
    var pinNuevo by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Ajustes", fontSize = 28.sp)

        Text("Ciudad para la temperatura: ${a.ciudad.ifEmpty { "(ninguna)" }}", fontSize = 18.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(busqueda, { busqueda = it }, label = { Text("Buscar ciudad") }, modifier = Modifier.weight(1f))
            Button(onClick = {
                buscando = true
                scope.launch {
                    resultados = OpenMeteo.parsearCiudades(descargar(OpenMeteo.urlCiudad(busqueda)))
                    sinResultados = resultados.isEmpty()
                    buscando = false
                }
            }, enabled = busqueda.isNotBlank() && !buscando) { Text(if (buscando) "…" else "Buscar") }
        }
        if (sinResultados) Text("No se ha encontrado (¿hay internet?)", fontSize = 16.sp)
        resultados.forEach { c ->
            TextButton(onClick = { a = a.copy(ciudad = c.nombre, lat = c.lat, lon = c.lon); resultados = emptyList() }) {
                Text("${c.nombre}${c.region?.let { " ($it)" }.orEmpty()}", fontSize = 18.sp)
            }
        }

        Text("Tarjeta DIA", fontSize = 22.sp)
        TipoTarjeta.entries.forEach { t ->
            Row(Modifier.selectable(a.tarjetaTipo == t) { a = a.copy(tarjetaTipo = t) }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(a.tarjetaTipo == t, onClick = { a = a.copy(tarjetaTipo = t) })
                Text(when (t) { TipoTarjeta.QR -> "Código QR"; TipoTarjeta.BARRAS -> "Código de barras"; TipoTarjeta.APP -> "Abrir la app de DIA" })
            }
        }
        if (a.tarjetaTipo != TipoTarjeta.APP) OutlinedTextField(
            a.tarjetaNumero, { a = a.copy(tarjetaNumero = it.trim()) }, label = { Text("Número de la tarjeta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            a.nombreAyuda, { a = a.copy(nombreAyuda = it) },
            label = { Text("Nombre de quien ayuda (sale en los errores)") }, modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(a.vozActivada, { a = a.copy(vozActivada = it) }); Spacer(Modifier.width(8.dp)); Text("Voz que anuncia las acciones")
        }

        OutlinedTextField(
            pinNuevo, { if (it.length <= 6 && it.all(Char::isDigit)) pinNuevo = it }, label = { Text("PIN nuevo (vacío = no cambiar)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation(),
            isError = pinNuevo.isNotEmpty() && !Pin.valido(pinNuevo), modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                var final = a.copy(nombreAyuda = a.nombreAyuda.ifBlank { "Fabri" })
                if (pinNuevo.isNotEmpty()) {
                    val sal = Pin.nuevaSal(); final = final.copy(pinSal = sal, pinHash = Pin.hash(pinNuevo, sal))
                }
                onGuardar(final)
            }, enabled = pinNuevo.isEmpty() || Pin.valido(pinNuevo)) { Text("Guardar") }
            OutlinedButton(onClick = onVolver) { Text("Volver") }
        }
    }
}
