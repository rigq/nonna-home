package com.fabri.nonna.mensajes

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LlamadaEntranteTest {
    private fun es(categoria: String?, fsi: Boolean = true, tipo: Int = LlamadaEntrante.TIPO_ENTRANTE) =
        LlamadaEntrante.esEntrante(categoria, fsi, tipo)

    @Test fun videollamadaDeWhatsAppEsEntrante() = assertTrue(es("call"))

    @Test fun sinTipoDeLlamadaPeroConPantallaCompletaCuenta() = assertTrue(es("call", tipo = 0))

    @Test fun sinPantallaCompletaNoSeAbre() = assertFalse(es("call", fsi = false))

    @Test fun llamadaEnCursoNoSeAbre() = assertFalse(es("call", tipo = LlamadaEntrante.TIPO_EN_CURSO))

    @Test fun llamadaFiltradaNoSeAbre() = assertFalse(es("call", tipo = LlamadaEntrante.TIPO_FILTRADA))

    @Test fun mensajesYPerdidasNoSonLlamadas() {
        assertFalse(es("msg"))
        assertFalse(es("missed_call"))
        assertFalse(es(null))
    }
}
