package com.fabri.nonna.seguridad

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinTest {
    @Test fun formatos() {
        assertTrue(Pin.valido("1234")); assertTrue(Pin.valido("123456"))
        assertFalse(Pin.valido("123")); assertFalse(Pin.valido("1234567")); assertFalse(Pin.valido("12a4")); assertFalse(Pin.valido(""))
    }
    @Test fun hashYComprobacion() {
        val sal = Pin.nuevaSal()
        val h = Pin.hash("2580", sal)
        assertTrue(Pin.comprobar("2580", sal, h)); assertFalse(Pin.comprobar("0000", sal, h))
        assertNotEquals(h, Pin.hash("2580", Pin.nuevaSal()))
    }
    @Test fun sinPinGuardadoNuncaValida() = assertFalse(Pin.comprobar("", "", ""))

    @Test fun bloqueoTrasCincoFallos() {
        var t = 0L
        val i = IntentosPin({ t })
        repeat(4) { i.fallo() }; assertFalse(i.bloqueado())
        i.fallo(); assertTrue(i.bloqueado()); assertEquals(30, i.segundosRestantes())
        t += 30_000; assertFalse(i.bloqueado())
    }
    @Test fun aciertoReiniciaFallos() {
        val i = IntentosPin({ 0L }); repeat(4) { i.fallo() }; i.acierto(); i.fallo()
        assertFalse(i.bloqueado())
    }

    @Test fun cincoToquesEnTresSegundos() {
        var t = 0L
        val c = ContadorToques(reloj = { t })
        repeat(4) { assertFalse(c.toque()); t += 500 }
        assertTrue(c.toque())
    }
    @Test fun toquesLentosNoAbren() {
        var t = 0L
        val c = ContadorToques(reloj = { t })
        repeat(10) { assertFalse(c.toque()); t += 1_000 }
    }
}
