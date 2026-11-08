package com.fabri.nonna.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.ui.components.BotonVolver
import com.fabri.nonna.ui.components.FotoFamiliar

@Composable
fun AdminFamiliaresScreen(
    familiares: List<Familiar>,
    onEditar: (Familiar?) -> Unit,
    onGuardarLista: (List<Familiar>) -> Unit,
    onVolver: () -> Unit
) {
    var aBorrar by remember { mutableStateOf<Familiar?>(null) }
    fun mover(i: Int, d: Int) {
        val j = i + d
        if (j !in familiares.indices) return
        onGuardarLista(familiares.toMutableList().apply { add(j, removeAt(i)) })
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { onEditar(null) }, modifier = Modifier.fillMaxWidth().height(64.dp)) { Text("+ Añadir familiar", fontSize = 22.sp) }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(familiares, key = { _, f -> f.id }) { i, f ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FotoFamiliar(f.nombre, f.rutaFoto, 56.dp)
                    Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        Text(f.nombre, fontSize = 22.sp); Text(f.telefono, fontSize = 16.sp)
                    }
                    TextButton(onClick = { mover(i, -1) }) { Text("↑", fontSize = 24.sp) }
                    TextButton(onClick = { mover(i, 1) }) { Text("↓", fontSize = 24.sp) }
                    TextButton(onClick = { onEditar(f) }) { Text("✏️", fontSize = 24.sp) }
                    TextButton(onClick = { aBorrar = f }) { Text("🗑", fontSize = 24.sp) }
                }
            }
        }
        BotonVolver(onVolver)
    }
    aBorrar?.let { f ->
        AlertDialog(
            onDismissRequest = { aBorrar = null },
            title = { Text("¿Borrar a ${f.nombre}?") },
            confirmButton = { TextButton(onClick = { onGuardarLista(familiares - f); aBorrar = null }) { Text("Borrar") } },
            dismissButton = { TextButton(onClick = { aBorrar = null }) { Text("Cancelar") } }
        )
    }
}
