package com.fabri.nonna.mensajes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BandejaMensajes {
    private val _lista = MutableStateFlow<List<MensajeNuevo>>(emptyList())
    val lista: StateFlow<List<MensajeNuevo>> = _lista.asStateFlow()

    private val categoriasIgnoradas = setOf("call", "progress", "sys", "err", "service", "missed_call")
    private val contador = Regex("""\s*\(\d+\s+mensajes?\)\s*$""", RegexOption.IGNORE_CASE)

    fun publicar(n: NotifCruda) {
        if (n.esResumen || n.categoria in categoriasIgnoradas || n.titulo.equals("WhatsApp", ignoreCase = true)) return
        val ultimo = n.mensajes.lastOrNull()
        val remitente = ultimo?.first?.takeIf { it.isNotBlank() } ?: n.titulo.orEmpty()
        val texto = (ultimo?.second ?: n.texto).orEmpty().trim()
        if (remitente.isBlank() && texto.isBlank()) return
        val conversacion = (n.conversacion ?: n.titulo ?: remitente).replace(contador, "").trim()
        val nuevo = MensajeNuevo(
            clave = n.clave, conversacion = conversacion, remitente = remitente, texto = texto,
            tipo = tipoDe(texto), esGrupo = n.esGrupo, hora = n.hora, cantidad = maxOf(1, n.mensajes.size)
        )
        _lista.update { l -> (l.filterNot { it.clave == n.clave } + nuevo).sortedByDescending { it.hora } }
    }

    fun quitar(clave: String) = _lista.update { l -> l.filterNot { it.clave == clave } }
    fun vaciar() { _lista.value = emptyList() }

    private fun tipoDe(t: String): TipoMensaje {
        val s = t.lowercase()
        return when {
            s.startsWith("🎤") || "mensaje de voz" in s || s.startsWith("audio") -> TipoMensaje.VOZ
            s.startsWith("📷") || s == "foto" || s.startsWith("foto ") -> TipoMensaje.FOTO
            s.startsWith("🎥") || s.startsWith("🎬") || s.startsWith("vídeo") || s.startsWith("video") -> TipoMensaje.VIDEO
            else -> TipoMensaje.TEXTO
        }
    }
}
