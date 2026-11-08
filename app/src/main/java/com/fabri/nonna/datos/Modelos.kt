package com.fabri.nonna.datos

import kotlinx.serialization.Serializable

@Serializable
data class Familiar(
    val id: String,
    val nombre: String,
    val telefono: String,
    val rutaFoto: String? = null,
    val orden: Int = 0
)

fun inicial(nombre: String): String =
    nombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

enum class TipoTarjeta { QR, BARRAS, APP }

@Serializable
data class Ajustes(
    val ciudad: String = "",
    val lat: Double? = null,
    val lon: Double? = null,
    val tarjetaNumero: String = "",
    val tarjetaTipo: TipoTarjeta = TipoTarjeta.QR,
    val vozActivada: Boolean = true,
    val nombreAyuda: String = "Fabri",
    val pinHash: String = "",
    val pinSal: String = "",
    val configurado: Boolean = false
)

@Serializable
data class TiempoCache(val tempC: Double, val codigo: Int, val horaMs: Long)
