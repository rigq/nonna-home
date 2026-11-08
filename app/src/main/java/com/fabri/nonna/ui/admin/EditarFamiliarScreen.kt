package com.fabri.nonna.ui.admin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fabri.nonna.acciones.Telefono
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.ui.components.FotoFamiliar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun EditarFamiliarScreen(
    familiar: Familiar?,
    onGuardar: (String, String, Uri?) -> Unit,
    onCancelar: () -> Unit,
    onEliminar: (() -> Unit)? = null
) {
    var nombre by remember { mutableStateOf(familiar?.nombre.orEmpty()) }
    var telefono by remember { mutableStateOf(familiar?.telefono ?: "+34") }
    var foto by remember { mutableStateOf<Uri?>(null) }
    var guardando by remember { mutableStateOf(false) }
    var confirmarBorrado by remember { mutableStateOf(false) }
    val elegir = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { foto = it ?: foto }
    val telOk = Telefono.normalizar(telefono).length >= 11
    val puedeGuardar = nombre.isNotBlank() && telOk && !guardando

    PantallaAdmin(
        if (familiar == null) "Nuevo familiar" else "Editar familiar",
        onCancelar,
        acciones = {
            TextButton(onClick = { guardando = true; onGuardar(nombre.trim(), telefono.trim(), foto) }, enabled = puedeGuardar) {
                Text("Guardar")
            }
        }
    ) { relleno ->
        Column(
            Modifier.padding(relleno).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.clip(CircleShape).clickable {
                    elegir.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    if (foto != null) VistaPrevia(foto!!, nombre) else FotoFamiliar(nombre.ifBlank { "?" }, familiar?.rutaFoto, 112.dp)
                }
                TextButton(onClick = { elegir.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text(if (foto == null && familiar?.rutaFoto == null) "Añadir foto" else "Cambiar foto")
                }
            }
            OutlinedTextField(
                nombre, { nombre = it },
                label = { Text("Nombre") },
                supportingText = { Text("Como lo verá ella en su pantalla") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                modifier = margenCampo.fillMaxWidth()
            )
            OutlinedTextField(
                telefono, { telefono = it },
                label = { Text("Teléfono") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                isError = !telOk,
                supportingText = {
                    Text(if (!telOk) "Número incompleto: incluye el prefijo (+34…)" else "Debe estar guardado también en Contactos para la videollamada directa")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                modifier = margenCampo.fillMaxWidth()
            )
            if (familiar != null && onEliminar != null) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { confirmarBorrado = true }, modifier = margenCampo) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("  Eliminar familiar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (confirmarBorrado && familiar != null && onEliminar != null) TemaAdmin {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("¿Borrar a ${familiar.nombre}?") },
            text = { Text("Dejará de salir en la pantalla Familia.") },
            confirmButton = { TextButton(onClick = { confirmarBorrado = false; onEliminar() }) { Text("Borrar") } },
            dismissButton = { TextButton(onClick = { confirmarBorrado = false }) { Text("Cancelar") } }
        )
    }
}

/** Muestra la foto recién elegida (aún sin guardar) reducida, para no cargar la imagen entera. */
@Composable
private fun VistaPrevia(uri: Uri, nombre: String) {
    val ctx = LocalContext.current
    val bmp by produceState<Bitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val r = ctx.contentResolver
                val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                r.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o) }
                var escala = 1
                while (o.outWidth / (escala * 2) >= 300 && o.outHeight / (escala * 2) >= 300) escala *= 2
                r.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = escala }) }
            }.getOrNull()
        }
    }
    val b = bmp
    if (b != null) Image(b.asImageBitmap(), nombre, Modifier.size(112.dp).clip(CircleShape), contentScale = ContentScale.Crop)
    else FotoFamiliar(nombre.ifBlank { "?" }, null, 112.dp)
}
