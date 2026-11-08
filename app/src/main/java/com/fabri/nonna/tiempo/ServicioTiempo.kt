package com.fabri.nonna.tiempo

import com.fabri.nonna.datos.Ajustes
import com.fabri.nonna.datos.Repositorio
import com.fabri.nonna.datos.TiempoCache

class ServicioTiempo(private val repo: Repositorio) {
    /** Sin ciudad o sin red: no hace nada y se mantiene la caché. */
    suspend fun refrescarSiToca(ajustes: Ajustes, cache: TiempoCache?) {
        val lat = ajustes.lat ?: return
        val lon = ajustes.lon ?: return
        val ahora = System.currentTimeMillis()
        if (!OpenMeteo.necesitaRefrescar(cache, ahora)) return
        OpenMeteo.parsearActual(descargar(OpenMeteo.urlActual(lat, lon)), ahora)?.let { repo.guardarTiempo(it) }
    }
}
