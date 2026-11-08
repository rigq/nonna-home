package com.fabri.nonna.datos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CodecTest {
    @Test fun familiaresIdaYVuelta() {
        val l = listOf(Familiar("1", "Fabri", "+34612345678", null, 0), Familiar("2", "María", "600111222", "/x.jpg", 1))
        assertEquals(l, Codec.familiares(Codec.texto(l)))
    }
    @Test fun familiaresCorruptosDanListaVacia() {
        assertEquals(emptyList<Familiar>(), Codec.familiares("{basura"))
        assertEquals(emptyList<Familiar>(), Codec.familiares(null))
    }
    @Test fun ajustesCorruptosDanValoresPorDefecto() = assertEquals(Ajustes(), Codec.ajustes("no-json"))
    @Test fun ajustesConCampoDesconocidoSeLeen() =
        assertEquals("Fabri", Codec.ajustes("""{"nombreAyuda":"Fabri","campoNuevo":1}""").nombreAyuda)
    @Test fun tiempoVacioEsNull() = assertNull(Codec.tiempo(""))

    @Test fun inicialNormal() = assertEquals("F", inicial("fabri"))
    @Test fun inicialConEspacios() = assertEquals("M", inicial("  maría"))
    @Test fun inicialVacia() = assertEquals("?", inicial(""))
    @Test fun inicialSoloEspacios() = assertEquals("?", inicial("   "))
}
