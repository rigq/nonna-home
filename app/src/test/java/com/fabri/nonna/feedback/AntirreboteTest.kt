package com.fabri.nonna.feedback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AntirreboteTest {
    private var ahora = 10_000L
    private val a = Antirrebote(ventanaMs = 1500, reloj = { ahora })

    @Test fun primerToquePasa() = assertTrue(a.intentar())

    @Test fun dobleToqueRapidoSeIgnora() {
        assertTrue(a.intentar())
        ahora += 200
        assertFalse(a.intentar())
    }

    @Test fun pasadaLaVentanaVuelveAPasar() {
        assertTrue(a.intentar())
        ahora += 1500
        assertTrue(a.intentar())
    }

    @Test fun toqueIgnoradoNoAlargaLaVentana() {
        assertTrue(a.intentar())
        ahora += 1000; assertFalse(a.intentar())
        ahora += 500; assertTrue(a.intentar())
    }

    @Test fun relojQueRetrocedeNoBloqueaParaSiempre() {
        assertTrue(a.intentar())
        ahora -= 60_000
        assertTrue(a.intentar())
    }

    @Test fun reiniciarPermiteElSiguienteToque() {
        assertTrue(a.intentar()); a.reiniciar(); ahora += 100
        assertTrue(a.intentar()); ahora += 100
        assertFalse(a.intentar())
    }
}
