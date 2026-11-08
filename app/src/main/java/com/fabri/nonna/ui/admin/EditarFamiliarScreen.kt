package com.fabri.nonna.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.nonna.acciones.Telefono
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.ui.components.FotoFamiliar

@Composable
fun EditarFamiliarScreen(familiar: Familiar?, onGuardar: (String, String, Uri?) -> Unit, onCancelar: () -> Unit) {
    var nombre by remember { mutableStateOf(familiar?.nombre.orEmpty()) }
    var telefono by remember { mutableStateOf(familiar?.telefono ?: "+34") }
    var foto by remember { mutableStateOf<Uri?>(null) }
    var guardando by remember { mutableStateOf(false) }
    val elegir = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { foto = it ?: foto }
    val telOk = Telefono.normalizar(telefono).length >= 11
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(if (familiar == null) "Nuevo familiar" else "Editar familiar", fontSize = 28.sp)
        FotoFamiliar(nombre, familiar?.rutaFoto, 96.dp)
        if (foto != null) Text("Foto nueva elegida ✅", fontSize = 18.sp)
        Button(onClick = { elegir.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Elegir foto") }
        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre (como lo verá ella)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            telefono, { telefono = it }, label = { Text("Teléfono con prefijo (+34…)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(),
            isError = !telOk, supportingText = { if (!telOk) Text("Número incompleto") }
        )
        Text("Para la videollamada directa, este número debe estar guardado también en Contactos del móvil.", fontSize = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { guardando = true; onGuardar(nombre.trim(), telefono.trim(), foto) },
                enabled = nombre.isNotBlank() && telOk && !guardando
            ) { Text("Guardar") }
            OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
        }
    }
}
