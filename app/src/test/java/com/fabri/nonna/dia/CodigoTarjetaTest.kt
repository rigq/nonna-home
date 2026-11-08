package com.fabri.nonna.dia

import com.fabri.nonna.datos.TipoTarjeta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CodigoTarjetaTest {
    @Test fun qr() {
        val m = CodigoTarjeta.matriz("1234567890123", TipoTarjeta.QR, 600, 600)
        assertNotNull(m); assertEquals(600, m!!.width)
    }
    @Test fun barras() = assertNotNull(CodigoTarjeta.matriz("1234567890123", TipoTarjeta.BARRAS, 900, 300))
    @Test fun vacioEsNull() = assertNull(CodigoTarjeta.matriz("  ", TipoTarjeta.QR, 600, 600))
    @Test fun modoAppEsNull() = assertNull(CodigoTarjeta.matriz("123", TipoTarjeta.APP, 600, 600))
}
