package com.fabri.nonna.acciones

sealed interface Accion {
    data object Camara : Accion
    data object YouTube : Accion
    data object ChatGptVoz : Accion
    data object AbrirWhatsApp : Accion
    data class WhatsAppChat(val telefono: String) : Accion
    data class WhatsAppVideo(val telefono: String) : Accion
}
