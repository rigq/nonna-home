package com.fabri.nonna.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabri.nonna.datos.Familiar
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

    PantallaAdmin(
        "Familiares", onVolver,
        botonFlotante = {
            FloatingActionButton(onClick = { onEditar(null) }) { Icon(Icons.Default.Add, contentDescription = "Añadir familiar") }
        }
    ) { relleno ->
        if (familiares.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) {
                Text("Aún no hay familiares.\nPulsa + para añadir el primero.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else LazyColumn(Modifier.padding(relleno), contentPadding = PaddingValues(bottom = 88.dp)) {
            itemsIndexed(familiares, key = { _, f -> f.id }) { i, f ->
                FilaFamiliar(
                    f,
                    puedeSubir = i > 0,
                    puedeBajar = i < familiares.lastIndex,
                    onEditar = { onEditar(f) },
                    onSubir = { mover(i, -1) },
                    onBajar = { mover(i, 1) },
                    onBorrar = { aBorrar = f }
                )
                HorizontalDivider()
            }
        }
    }

    aBorrar?.let { f ->
        TemaAdmin {
            AlertDialog(
                onDismissRequest = { aBorrar = null },
                title = { Text("¿Borrar a ${f.nombre}?") },
                text = { Text("Dejará de salir en la pantalla Familia.") },
                confirmButton = { TextButton(onClick = { onGuardarLista(familiares - f); aBorrar = null }) { Text("Borrar") } },
                dismissButton = { TextButton(onClick = { aBorrar = null }) { Text("Cancelar") } }
            )
        }
    }
}

@Composable
private fun FilaFamiliar(
    f: Familiar,
    puedeSubir: Boolean,
    puedeBajar: Boolean,
    onEditar: () -> Unit,
    onSubir: () -> Unit,
    onBajar: () -> Unit,
    onBorrar: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(f.nombre) },
        supportingContent = { Text(f.telefono) },
        leadingContent = { FotoFamiliar(f.nombre, f.rutaFoto, 40.dp) },
        trailingContent = {
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Opciones") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Editar") }, onClick = { menu = false; onEditar() })
                    DropdownMenuItem(text = { Text("Subir") }, enabled = puedeSubir, onClick = { menu = false; onSubir() })
                    DropdownMenuItem(text = { Text("Bajar") }, enabled = puedeBajar, onClick = { menu = false; onBajar() })
                    DropdownMenuItem(
                        text = { Text("Borrar", color = MaterialTheme.colorScheme.error) },
                        onClick = { menu = false; onBorrar() }
                    )
                }
            }
        },
        modifier = Modifier.clickable(onClick = onEditar)
    )
}
