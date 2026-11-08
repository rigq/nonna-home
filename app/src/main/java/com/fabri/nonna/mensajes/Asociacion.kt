package com.fabri.nonna.mensajes

import com.fabri.nonna.datos.Familiar
import java.text.Normalizer

object Asociacion {
    private val marcas = Regex("\\p{Mn}+")

    fun sinAcentos(s: String): String =
        Normalizer.normalize(s.trim().lowercase(), Normalizer.Form.NFD).replace(marcas, "")

    fun familiarDe(m: MensajeNuevo, familiares: List<Familiar>, nombresAgenda: Map<String, String>): Familiar? {
        if (m.esGrupo) return null
        val quien = sinAcentos(m.remitente)
        if (quien.isEmpty()) return null
        return familiares.firstOrNull { f -> nombresAgenda[f.id]?.let { sinAcentos(it) == quien } == true }
            ?: familiares.firstOrNull { sinAcentos(it.nombre) == quien }
    }

    fun nuevosDe(f: Familiar, mensajes: List<MensajeNuevo>, familiares: List<Familiar>, nombresAgenda: Map<String, String>): Int =
        mensajes.filter { familiarDe(it, familiares, nombresAgenda)?.id == f.id }.sumOf { it.cantidad }
}
