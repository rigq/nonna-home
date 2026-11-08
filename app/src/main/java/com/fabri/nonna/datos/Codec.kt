package com.fabri.nonna.datos

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object Codec {
    private val json = Json { ignoreUnknownKeys = true }

    fun familiares(s: String?): List<Familiar> =
        s?.let { runCatching { json.decodeFromString<List<Familiar>>(it) }.getOrNull() } ?: emptyList()
    fun texto(l: List<Familiar>): String = json.encodeToString(l)

    fun ajustes(s: String?): Ajustes =
        s?.let { runCatching { json.decodeFromString<Ajustes>(it) }.getOrNull() } ?: Ajustes()
    fun texto(a: Ajustes): String = json.encodeToString(a)

    fun tiempo(s: String?): TiempoCache? =
        s?.takeIf { it.isNotBlank() }?.let { runCatching { json.decodeFromString<TiempoCache>(it) }.getOrNull() }
    fun texto(t: TiempoCache): String = json.encodeToString(t)
}
