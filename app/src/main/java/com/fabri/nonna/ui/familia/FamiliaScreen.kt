package com.fabri.nonna.ui.familia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onVideo: (Familiar) -> Unit,
    onMensajes: (Familiar) -> Unit,
    onAbrirWhatsApp: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion),
        verticalArrangement = Arrangement.spacedBy(Medidas.separacion)
    ) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Medidas.separacion)) {
            item { cabecera() }
            items(familiares, key = { it.id }) { f ->
                TarjetaFamiliar(f, nuevosDe(f), onVideo = { onVideo(f) }, onMensajes = { onMensajes(f) })
            }
            item {
                BotonGrande("💬", "ABRIR WHATSAPP", "Ver todos los chats", Colores.whatsapp, Modifier.fillMaxWidth()) { onAbrirWhatsApp() }
            }
        }
        BotonVolver(onVolver)
    }
}
