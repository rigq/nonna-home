package com.fabri.nonna.acciones

enum class Resultado { Ok, PlanB, Error }

class Lanzador(private val abrir: (Destino) -> Boolean) {

    companion object {
        const val ACCION_CAMARA_FOTO = "android.media.action.STILL_IMAGE_CAMERA"
        val PALABRAS_VOZ = listOf("voice", "voz")
        /** Enlace que la app de ChatGPT abre directamente en modo voz (comprobado en el Redmi, nov 2026).
         *  No necesita ser la pantalla de inicio, a diferencia del atajo. */
        const val URL_VOZ_CHATGPT = "https://chatgpt.com/voice"
    }

    fun planes(a: Accion): List<Destino> = when (a) {
        Accion.Camara -> listOf(Destino.AccionSistema(ACCION_CAMARA_FOTO), Destino.CamaraPorDefecto)
        Accion.YouTube -> listOf(Destino.AbrirApp(Paquetes.YOUTUBE))
        Accion.ChatGptVoz -> listOf(
            Destino.Ver(URL_VOZ_CHATGPT, Paquetes.CHATGPT),
            Destino.Atajo(Paquetes.CHATGPT, PALABRAS_VOZ), Destino.AbrirApp(Paquetes.CHATGPT))
        Accion.AbrirWhatsApp -> listOf(Destino.AbrirApp(Paquetes.WHATSAPP))
        is Accion.WhatsAppChat -> chat(a.telefono) + Destino.AbrirApp(Paquetes.WHATSAPP)
        is Accion.WhatsAppVideo -> listOfNotNull(
            Destino.VideoWhatsApp(a.telefono).takeIf { Telefono.normalizar(a.telefono).isNotEmpty() }
        ) + chat(a.telefono) + Destino.AbrirApp(Paquetes.WHATSAPP)
        is Accion.AbrirMensaje -> listOf(Destino.Notificacion(a.clave)) +
            chat(a.telefonoFamiliar.orEmpty()) + Destino.AbrirApp(Paquetes.WHATSAPP)
        Accion.TarjetaDiaApp -> listOf(Destino.Atajo(Paquetes.DIA, listOf("tarjeta", "card", "club")), Destino.AbrirApp(Paquetes.DIA))
    }

    private fun chat(tel: String): List<Destino> {
        val n = Telefono.paraWaMe(tel)
        return if (n.isEmpty()) emptyList() else listOf(Destino.Ver("https://wa.me/$n", Paquetes.WHATSAPP))
    }

    fun ejecutar(a: Accion): Resultado {
        planes(a).forEachIndexed { i, d ->
            val ok = try { abrir(d) } catch (e: Exception) { false }
            if (ok) return if (i == 0) Resultado.Ok else Resultado.PlanB
        }
        return Resultado.Error
    }
}
