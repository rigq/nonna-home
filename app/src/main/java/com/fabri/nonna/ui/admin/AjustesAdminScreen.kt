package com.fabri.nonna.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
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
    val pinOk = pinNuevo.isEmpty() || Pin.valido(pinNuevo)

    fun buscar() {
        if (busqueda.isBlank() || buscando) return
        buscando = true
        scope.launch {
            resultados = OpenMeteo.parsearCiudades(descargar(OpenMeteo.urlCiudad(busqueda)))
            sinResultados = resultados.isEmpty()
            buscando = false
        }
    }

    PantallaAdmin(
        "Ajustes", onVolver,
        acciones = {
            TextButton(enabled = pinOk, onClick = {
                var final = a.copy(nombreAyuda = a.nombreAyuda.ifBlank { "Fabri" })
                if (pinNuevo.isNotEmpty()) {
                    val sal = Pin.nuevaSal(); final = final.copy(pinSal = sal, pinHash = Pin.hash(pinNuevo, sal))
                }
                onGuardar(final)
            }) { Text("Guardar") }
        }
    ) { relleno ->
        Column(
            Modifier.padding(relleno).verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ---------- Temperatura ----------
            TituloSeccion("Temperatura")
            ListItem(
                headlineContent = { Text(a.ciudad.ifEmpty { "Sin ciudad" }) },
                supportingContent = { Text("Ciudad elegida") },
                leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) }
            )
            OutlinedTextField(
                busqueda, { busqueda = it },
                label = { Text("Buscar otra ciudad") },
                singleLine = true,
                trailingIcon = {
                    if (buscando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else IconButton(onClick = ::buscar, enabled = busqueda.isNotBlank()) { Icon(Icons.Default.Search, contentDescription = "Buscar") }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { buscar() }),
                supportingText = if (sinResultados) ({ Text("No se ha encontrado (¿hay internet?)") }) else null,
                isError = sinResultados,
                modifier = margenCampo.fillMaxWidth()
            )
            resultados.forEach { c ->
                ListItem(
                    headlineContent = { Text(c.nombre) },
                    supportingContent = c.region?.let { { Text(it) } },
                    modifier = Modifier.clickable { a = a.copy(ciudad = c.nombre, lat = c.lat, lon = c.lon); resultados = emptyList() }
                )
            }

            HorizontalDivider(Modifier.padding(top = 12.dp))

            // ---------- Tarjeta DIA ----------
            TituloSeccion("Tarjeta DIA")
            TipoTarjeta.entries.forEach { t ->
                Row(
                    Modifier.fillMaxWidth().selectable(a.tarjetaTipo == t) { a = a.copy(tarjetaTipo = t) }.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(a.tarjetaTipo == t, onClick = null, modifier = Modifier.padding(12.dp))
                    Text(when (t) {
                        TipoTarjeta.QR -> "Mostrar código QR"
                        TipoTarjeta.BARRAS -> "Mostrar código de barras"
                        TipoTarjeta.APP -> "Abrir la app de DIA"
                    })
                }
            }
            if (a.tarjetaTipo != TipoTarjeta.APP) OutlinedTextField(
                a.tarjetaNumero, { a = a.copy(tarjetaNumero = it.trim()) },
                label = { Text("Número de la tarjeta") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = margenCampo.fillMaxWidth()
            )

            HorizontalDivider(Modifier.padding(top = 12.dp))

            // ---------- General ----------
            TituloSeccion("General")
            OutlinedTextField(
                a.nombreAyuda, { a = a.copy(nombreAyuda = it) },
                label = { Text("Persona de ayuda") },
                supportingText = { Text("Su nombre sale si algo falla") },
                singleLine = true,
                modifier = margenCampo.fillMaxWidth()
            )
            ListItem(
                headlineContent = { Text("Voz") },
                supportingContent = { Text("Anuncia en voz alta cada botón") },
                trailingContent = { Switch(a.vozActivada, onCheckedChange = null) },
                modifier = Modifier.clickable { a = a.copy(vozActivada = !a.vozActivada) }
            )

            HorizontalDivider(Modifier.padding(top = 4.dp))

            // ---------- Seguridad ----------
            TituloSeccion("Seguridad")
            OutlinedTextField(
                pinNuevo, { if (it.length <= 6 && it.all(Char::isDigit)) pinNuevo = it },
                label = { Text("Nuevo PIN") },
                supportingText = { Text(if (pinOk) "Déjalo vacío para no cambiarlo" else "De 4 a 6 números") },
                singleLine = true,
                isError = !pinOk,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = margenCampo.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Los cambios se aplican al pulsar Guardar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = margenCampo
            )
        }
    }
}
