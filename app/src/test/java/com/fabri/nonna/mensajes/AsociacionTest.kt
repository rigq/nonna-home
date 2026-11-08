package com.fabri.nonna.mensajes

import com.fabri.nonna.datos.Familiar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AsociacionTest {
    private val fabri = Familiar("1", "Fabri", "+34612345678")
    private val maria = Familiar("2", "María", "600111222")
    private val familia = listOf(fabri, maria)
    private val agenda = mapOf("1" to "Fabrizio Nieto", "2" to "Maria Hija")

    private fun m(remitente: String, grupo: Boolean = false, cantidad: Int = 1) =
        MensajeNuevo("k$remitente", remitente, remitente, "hola", TipoMensaje.TEXTO, grupo, 1, cantidad)

    @Test fun porNombreDeAgenda() = assertEquals(fabri, Asociacion.familiarDe(m("Fabrizio Nieto"), familia, agenda))
    @Test fun sinAcentosNiMayusculas() = assertEquals(maria, Asociacion.familiarDe(m("MARÍA HIJA"), familia, agenda))
    @Test fun porNombreDelLauncherSiNoHayAgenda() = assertEquals(maria, Asociacion.familiarDe(m("maria"), familia, emptyMap()))
    @Test fun desconocidoEsNull() = assertNull(Asociacion.familiarDe(m("Banco"), familia, agenda))
    @Test fun grupoNoSeAsocia() = assertNull(Asociacion.familiarDe(m("Fabrizio Nieto", grupo = true), familia, agenda))
    @Test fun cuentaNuevos() =
        assertEquals(3, Asociacion.nuevosDe(fabri, listOf(m("Fabrizio Nieto", cantidad = 3), m("Banco")), familia, agenda))
}
