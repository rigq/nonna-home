package com.fabri.nonna.ui.familia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.ui.components.BotonGrande
import com.fabri.nonna.ui.components.BotonVolver
import com.fabri.nonna.ui.theme.Colores
import com.fabri.nonna.ui.theme.Medidas

@Composable
fun FamiliaScreen(
    familiares: List<Familiar>,
    nuevosDe: (Familiar) -> Int,
    cabecera: @Composable () -> Unit,
    onElegir: (Familiar) -> Unit,
    onAbrirWhatsApp: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion),
        verticalArrangement = Arrangement.spacedBy(Medidas.separacion)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Medidas.separacion),
            horizontalArrangement = Arrangement.spacedBy(Medidas.separacion)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { cabecera() }
            items(familiares, key = { it.id }) { f -> TarjetaFamiliar(f, nuevosDe(f)) { onElegir(f) } }
            item(span = { GridItemSpan(maxLineSpan) }) {
                BotonGrande("💬", "ABRIR WHATSAPP", "Ver todos los chats", Colores.whatsapp, Modifier.fillMaxWidth()) { onAbrirWhatsApp() }
            }
        }
        BotonVolver(onVolver)
    }
}
