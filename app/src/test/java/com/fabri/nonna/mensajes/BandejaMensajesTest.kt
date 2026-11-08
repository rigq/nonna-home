package com.fabri.nonna.mensajes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BandejaMensajesTest {
    private val b = BandejaMensajes()

    private fun individual(clave: String, quien: String, vararg textos: String, hora: Long = 1) = NotifCruda(
        clave = clave, esResumen = false, categoria = "msg", conversacion = null, esGrupo = false,
        mensajes = textos.map { quien to it }, titulo = quien, texto = textos.lastOrNull(), hora = hora
    )

    @Test fun chatIndividual() {
        b.publicar(individual("k1", "Fabri", "¿Has comido ya?"))
        val m = b.lista.value.single()
        assertEquals("Fabri", m.conversacion); assertEquals("Fabri", m.remitente)
        assertEquals("¿Has comido ya?", m.texto); assertEquals(TipoMensaje.TEXTO, m.tipo)
        assertEquals("¿Has comido ya?", m.resumen)
    }

    @Test fun mismaNotificacionActualizadaNoDuplica() {
        b.publicar(individual("k1", "Fabri", "Hola", hora = 1))
        b.publicar(individual("k1", "Fabri", "Hola", "¿Vienes?", hora = 2))
        val m = b.lista.value.single()
        assertEquals("¿Vienes?", m.texto); assertEquals(2, m.cantidad)
    }

    @Test fun resumenDeGrupoSeIgnora() {
        b.publicar(individual("r", "WhatsApp", "3 mensajes de 2 chats").copy(esResumen = true))
        assertTrue(b.lista.value.isEmpty())
    }

    @Test fun tituloWhatsAppSeIgnora() {
        b.publicar(individual("r2", "WhatsApp", "5 mensajes nuevos"))
        assertTrue(b.lista.value.isEmpty())
    }

    @Test fun llamadasYProgresoSeIgnoran() {
        b.publicar(individual("c", "Fabri", "Llamada entrante").copy(categoria = "call"))
        b.publicar(individual("p", "WhatsApp", "Copia de seguridad").copy(categoria = "progress"))
        assertTrue(b.lista.value.isEmpty())
    }

    @Test fun grupoConContadorEnTitulo() {
        b.publicar(NotifCruda("g", false, "msg", "Familia (3 mensajes)", true, listOf("María" to "Feliz día"), "Familia", null, 5))
        val m = b.lista.value.single()
        assertEquals("Familia", m.conversacion)
        assertEquals("Familia · María: Feliz día", m.resumen)
    }

    @Test fun tiposDeMensaje() {
        b.publicar(individual("a", "Ana", "🎤 Mensaje de voz (0:12)", hora = 1))
        b.publicar(individual("f", "Luis", "📷 Foto", hora = 2))
        b.publicar(individual("v", "Eva", "🎥 Vídeo", hora = 3))
        val tipos = b.lista.value.associate { it.remitente to it.tipo }
        assertEquals(TipoMensaje.VOZ, tipos["Ana"]); assertEquals(TipoMensaje.FOTO, tipos["Luis"]); assertEquals(TipoMensaje.VIDEO, tipos["Eva"])
        assertEquals("🎤 Mensaje de voz", b.lista.value.first { it.remitente == "Ana" }.resumen)
    }

    @Test fun quitarLaElimina() {
        b.publicar(individual("k1", "Fabri", "Hola")); b.quitar("k1")
        assertTrue(b.lista.value.isEmpty())
    }

    @Test fun masRecientePrimero() {
        b.publicar(individual("a", "Ana", "1", hora = 1)); b.publicar(individual("b", "Luis", "2", hora = 9))
        assertEquals("Luis", b.lista.value.first().remitente)
    }

    @Test fun sinMensajesUsaTituloYTexto() {
        b.publicar(NotifCruda("x", false, null, null, false, emptyList(), "Fabri", "Hola abuela", 1))
        assertEquals("Hola abuela", b.lista.value.single().texto)
    }

    @Test fun vaciaSeIgnora() {
        b.publicar(NotifCruda("x", false, null, null, false, emptyList(), null, null, 1))
        assertTrue(b.lista.value.isEmpty())
    }
}
