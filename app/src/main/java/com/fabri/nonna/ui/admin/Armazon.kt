package com.fabri.nonna.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Zona de administración: diseño Android normal y sobrio, pensado para quien configura el móvil,
 * no para la abuela. Nada de botones gigantes ni colores fuertes.
 */
private val coloresAdmin = lightColorScheme(
    primary = Color(0xFF1F5FAD),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E3F8),
    onPrimaryContainer = Color(0xFF0B2545),
    secondaryContainer = Color(0xFFE3E7EE),
    background = Color(0xFFFAFAFC),
    surface = Color(0xFFFAFAFC),
    surfaceContainer = Color(0xFFF0F2F6),
    onSurfaceVariant = Color(0xFF5F6670),
    outlineVariant = Color(0xFFDDE1E7),
    error = Color(0xFFC62828)
)

val verdeOk = Color(0xFF2E7D32)

@Composable
fun TemaAdmin(content: @Composable () -> Unit) = MaterialTheme(colorScheme = coloresAdmin, content = content)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAdmin(
    titulo: String,
    onVolver: () -> Unit,
    acciones: @Composable RowScope.() -> Unit = {},
    botonFlotante: @Composable () -> Unit = {},
    contenido: @Composable (PaddingValues) -> Unit
) = TemaAdmin {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
                actions = acciones
            )
        },
        floatingActionButton = botonFlotante,
        containerColor = MaterialTheme.colorScheme.background,
        content = contenido
    )
}

@Composable
fun FilaAdmin(
    titulo: String,
    subtitulo: String? = null,
    icono: ImageVector? = null,
    colorIcono: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    final: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(titulo) },
        supportingContent = subtitulo?.let { { Text(it) } },
        leadingContent = icono?.let { { Icon(it, contentDescription = null, tint = colorIcono) } },
        trailingContent = final,
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    )
}

@Composable
fun TituloSeccion(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)
    )
}

/** Margen horizontal estándar para campos de formulario dentro de una columna. */
val margenCampo = Modifier.padding(horizontal = 16.dp)
