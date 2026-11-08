package com.fabri.nonna.acciones

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TelefonoTest {
    @Test fun formatosEspanolesSonElMismo() {
        val esperado = "34612345678"
        assertEquals(esperado, Telefono.normalizar("+34 612 345 678"))
        assertEquals(esperado, Telefono.normalizar("612-345-678"))
        assertEquals(esperado, Telefono.normalizar("0034612345678"))
        assertEquals(esperado, Telefono.normalizar("34612345678"))
        assertEquals(esperado, Telefono.normalizar("(+34) 612.345.678"))
    }

    @Test fun jidDeWhatsApp() =
        assertEquals("34612345678", Telefono.normalizar("34612345678@s.whatsapp.net".substringBefore("@")))

    @Test fun vacioNoCoincideConNada() {
        assertEquals("", Telefono.normalizar(""))
        assertEquals("", Telefono.normalizar("abc"))
        assertFalse(Telefono.mismo("", ""))
        assertFalse(Telefono.mismo("abc", "+34 612 345 678"))
    }

    @Test fun mismoNumeroEnFormatosDistintos() =
        assertTrue(Telefono.mismo("612 345 678", "+34612345678"))

    @Test fun waMeSinMas() = assertEquals("34612345678", Telefono.paraWaMe("+34 612 345 678"))
}
