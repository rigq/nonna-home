package com.fabri.nonna.feedback

import android.os.SystemClock

/** Deja pasar un toque y bloquea los siguientes durante [ventanaMs]. */
class Antirrebote(
    private val ventanaMs: Long = 1500,
    private val reloj: () -> Long = { SystemClock.elapsedRealtime() }
) {
    private var ultimo: Long? = null

    @Synchronized
    fun intentar(): Boolean {
        val ahora = reloj()
        val u = ultimo
        if (u != null && ahora >= u && ahora - u < ventanaMs) return false
        ultimo = ahora
        return true
    }

    @Synchronized
    fun reiniciar() { ultimo = null }
}
