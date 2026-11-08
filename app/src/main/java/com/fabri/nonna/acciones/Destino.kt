package com.fabri.nonna.acciones

/** Descripción pura de "qué abrir". El Ejecutor la convierte en Intent. */
sealed interface Destino {
    data class Ver(val uri: String, val paquete: String? = null, val mime: String? = null) : Destino
    data class AccionSistema(val action: String) : Destino
    data object CamaraPorDefecto : Destino
    data class AbrirApp(val paquete: String) : Destino
    data class Atajo(val paquete: String, val palabras: List<String>) : Destino
    data class VideoWhatsApp(val telefono: String) : Destino
    data class Notificacion(val clave: String) : Destino
}
