package com.fabri.nonna.tiempo

import com.fabri.nonna.datos.TiempoCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.roundToInt

@Serializable
data class Ciudad(val nombre: String, val lat: Double, val lon: Double, val region: String? = null)

@Serializable private data class Actual(
    @SerialName("temperature_2m") val temperatura: Double? = null,
    @SerialName("weather_code") val codigo: Int? = null
)
@Serializable private data class RespuestaActual(val current: Actual? = null)
@Serializable private data class CiudadApi(
    val name: String, val latitude: Double, val longitude: Double, val admin1: String? = null
)
@Serializable private data class RespuestaCiudades(val results: List<CiudadApi>? = null)

object OpenMeteo {
    private val json = Json { ignoreUnknownKeys = true }
    private const val TREINTA_MIN = 30 * 60_000L

    fun urlActual(lat: Double, lon: Double) =
        "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code&timezone=auto"

    fun urlCiudad(nombre: String) =
        "https://geocoding-api.open-meteo.com/v1/search?name=${URLEncoder.encode(nombre.trim(), "UTF-8")}&language=es&count=5"

    fun parsearActual(texto: String?, ahoraMs: Long): TiempoCache? {
        val r = texto?.let { runCatching { json.decodeFromString<RespuestaActual>(it) }.getOrNull() } ?: return null
        val actual = r.current ?: return null
        val t = actual.temperatura ?: return null
        return TiempoCache(t, actual.codigo ?: -1, ahoraMs)
    }

    fun parsearCiudades(texto: String?): List<Ciudad> =
        texto?.let { runCatching { json.decodeFromString<RespuestaCiudades>(it) }.getOrNull() }
            ?.results.orEmpty().map { Ciudad(it.name, it.latitude, it.longitude, it.admin1) }

    fun icono(codigo: Int): String = when (codigo) {
        0 -> "☀️"
        1, 2 -> "⛅"
        3 -> "☁️"
        45, 48 -> "🌫️"
        in 51..67, in 80..82 -> "🌧️"
        in 71..77, 85, 86 -> "❄️"
        in 95..99 -> "⛈️"
        else -> "🌡️"
    }

    fun texto(c: TiempoCache) = "${c.tempC.roundToInt()}°C ${icono(c.codigo)}"

    fun necesitaRefrescar(c: TiempoCache?, ahoraMs: Long): Boolean =
        c == null || ahoraMs < c.horaMs || ahoraMs - c.horaMs >= TREINTA_MIN
}

suspend fun descargar(url: String): String? = withContext(Dispatchers.IO) {
    runCatching {
        val con = URL(url).openConnection() as HttpURLConnection
        con.connectTimeout = 8_000; con.readTimeout = 8_000
        try { if (con.responseCode == 200) con.inputStream.bufferedReader().readText() else null }
        finally { con.disconnect() }
    }.getOrNull()
}
