package com.fabri.nonna.mensajes

data class NotifCruda(
    val clave: String,
    val esResumen: Boolean,
    val categoria: String?,
    val conversacion: String?,
    val esGrupo: Boolean,
    val mensajes: List<Pair<String, String>>,
    val titulo: String?,
    val texto: String?,
    val hora: Long
)

enum class TipoMensaje { TEXTO, VOZ, FOTO, VIDEO }

data class MensajeNuevo(
    val clave: String,
    val conversacion: String,
    val remitente: String,
    val texto: String,
    val tipo: TipoMensaje,
    val esGrupo: Boolean,
    val hora: Long,
    val cantidad: Int
) {
    private val cuerpo: String get() = when (tipo) {
        TipoMensaje.VOZ -> "🎤 Mensaje de voz"
        TipoMensaje.FOTO -> "📷 Foto"
        TipoMensaje.VIDEO -> "🎬 Vídeo"
        TipoMensaje.TEXTO -> texto
    }
    val resumen: String get() = if (esGrupo) "$conversacion · $remitente: $cuerpo" else cuerpo
}
