package com.fabri.nonna.tiempo

import com.fabri.nonna.datos.TiempoCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoTest {
    @Test fun parseaActual() {
        val json = """{"latitude":40.4,"current":{"time":"2026-11-08T10:45","temperature_2m":14.3,"weather_code":3}}"""
        assertEquals(TiempoCache(14.3, 3, 99), OpenMeteo.parsearActual(json, 99))
    }
    @Test fun faltaTemperaturaEsNull() = assertNull(OpenMeteo.parsearActual("""{"current":{"weather_code":3}}""", 1))
    @Test fun faltaCurrentEsNull() = assertNull(OpenMeteo.parsearActual("""{"latitude":40.4}""", 1))
    @Test fun htmlDeErrorEsNull() = assertNull(OpenMeteo.parsearActual("<html>502 Bad Gateway</html>", 1))
    @Test fun nullEsNull() = assertNull(OpenMeteo.parsearActual(null, 1))
    @Test fun sinCodigoUsaDesconocido() =
        assertEquals(-1, OpenMeteo.parsearActual("""{"current":{"temperature_2m":10.0}}""", 1)!!.codigo)

    @Test fun ciudades() {
        val json = """{"results":[{"name":"Madrid","latitude":40.41,"longitude":-3.70,"admin1":"Madrid","country":"España"}]}"""
        assertEquals(listOf(Ciudad("Madrid", 40.41, -3.70, "Madrid")), OpenMeteo.parsearCiudades(json))
    }
    @Test fun sinResultadosListaVacia() = assertEquals(emptyList<Ciudad>(), OpenMeteo.parsearCiudades("""{"generationtime_ms":0.5}"""))
    @Test fun ciudadesBasura() = assertEquals(emptyList<Ciudad>(), OpenMeteo.parsearCiudades("xx"))

    @Test fun iconos() {
        assertEquals("☀️", OpenMeteo.icono(0)); assertEquals("⛅", OpenMeteo.icono(2)); assertEquals("☁️", OpenMeteo.icono(3))
        assertEquals("🌫️", OpenMeteo.icono(45)); assertEquals("🌧️", OpenMeteo.icono(61)); assertEquals("🌧️", OpenMeteo.icono(81))
        assertEquals("❄️", OpenMeteo.icono(73)); assertEquals("⛈️", OpenMeteo.icono(95)); assertEquals("🌡️", OpenMeteo.icono(-1))
    }
    @Test fun textoRedondea() = assertEquals("14°C ☁️", OpenMeteo.texto(TiempoCache(14.4, 3, 0)))
    @Test fun textoNegativo() = assertEquals("-2°C ❄️", OpenMeteo.texto(TiempoCache(-2.4, 71, 0)))

    @Test fun refrescoCada30Min() {
        assertTrue(OpenMeteo.necesitaRefrescar(null, 0))
        assertFalse(OpenMeteo.necesitaRefrescar(TiempoCache(1.0, 0, 0), 29 * 60_000L))
        assertTrue(OpenMeteo.necesitaRefrescar(TiempoCache(1.0, 0, 0), 30 * 60_000L))
        assertTrue(OpenMeteo.necesitaRefrescar(TiempoCache(1.0, 0, 10 * 60_000L), 0))
    }
    @Test fun urlConPuntoDecimal() =
        assertTrue(OpenMeteo.urlActual(40.41, -3.7).contains("latitude=40.41&longitude=-3.7"))
}
