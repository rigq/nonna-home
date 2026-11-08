package com.fabri.nonna.acciones

import org.junit.Assert.assertEquals
import org.junit.Test

class LanzadorTest {
    private val intentados = mutableListOf<Destino>()
    private fun lanzadorQueAcepta(vararg ok: Destino) = Lanzador { d -> intentados += d; d in ok }

    @Test fun camaraPlanA() {
        val l = lanzadorQueAcepta(Destino.AccionSistema(Lanzador.ACCION_CAMARA_FOTO))
        assertEquals(Resultado.Ok, l.ejecutar(Accion.Camara))
    }

    @Test fun camaraCaeAPlanB() {
        val l = lanzadorQueAcepta(Destino.CamaraPorDefecto)
        assertEquals(Resultado.PlanB, l.ejecutar(Accion.Camara))
        assertEquals(2, intentados.size)
    }

    @Test fun youtubeNoInstaladoEsError() {
        val l = lanzadorQueAcepta()
        assertEquals(Resultado.Error, l.ejecutar(Accion.YouTube))
    }

    @Test fun chatGptAbreDirectoElModoVoz() {
        val planes = Lanzador { false }.planes(Accion.ChatGptVoz)
        assertEquals(Destino.Ver(Lanzador.URL_VOZ_CHATGPT, Paquetes.CHATGPT), planes[0])
        assertEquals(Destino.Atajo(Paquetes.CHATGPT, Lanzador.PALABRAS_VOZ), planes[1])
        assertEquals(Destino.AbrirApp(Paquetes.CHATGPT), planes.last())
    }

    @Test fun chatGptSinModoVozAbreLaApp() {
        val l = lanzadorQueAcepta(Destino.AbrirApp(Paquetes.CHATGPT))
        assertEquals(Resultado.PlanB, l.ejecutar(Accion.ChatGptVoz))
    }

    @Test fun chatDeWhatsAppUsaWaMe() {
        val planes = Lanzador { false }.planes(Accion.WhatsAppChat("+34 612 345 678"))
        assertEquals(Destino.Ver("https://wa.me/34612345678", Paquetes.WHATSAPP), planes.first())
        assertEquals(Destino.AbrirApp(Paquetes.WHATSAPP), planes.last())
    }

    @Test fun videollamadaCaeAlChat() {
        val chat = Destino.Ver("https://wa.me/34612345678", Paquetes.WHATSAPP)
        val l = lanzadorQueAcepta(chat)
        assertEquals(Resultado.PlanB, l.ejecutar(Accion.WhatsAppVideo("612345678")))
    }

    @Test fun excepcionCuentaComoFallo() {
        val l = Lanzador { d -> if (d is Destino.AccionSistema) throw SecurityException() else true }
        assertEquals(Resultado.PlanB, l.ejecutar(Accion.Camara))
    }

    @Test fun telefonoVacioNoIntentaWaMe() {
        val planes = Lanzador { false }.planes(Accion.WhatsAppChat(""))
        assertEquals(listOf(Destino.AbrirApp(Paquetes.WHATSAPP)), planes)
    }

    @Test fun abrirMensajeDeFamiliar() {
        val planes = Lanzador { false }.planes(Accion.AbrirMensaje("k1", "612345678"))
        assertEquals(
            listOf(Destino.Notificacion("k1"), Destino.Ver("https://wa.me/34612345678", Paquetes.WHATSAPP), Destino.AbrirApp(Paquetes.WHATSAPP)),
            planes
        )
    }

    @Test fun abrirMensajeDeDesconocido() {
        val planes = Lanzador { false }.planes(Accion.AbrirMensaje("k1", null))
        assertEquals(listOf(Destino.Notificacion("k1"), Destino.AbrirApp(Paquetes.WHATSAPP)), planes)
    }

    @Test fun tarjetaDiaSinAtajoAbreLaApp() {
        val l = lanzadorQueAcepta(Destino.AbrirApp(Paquetes.DIA))
        assertEquals(Resultado.PlanB, l.ejecutar(Accion.TarjetaDiaApp))
    }
}
