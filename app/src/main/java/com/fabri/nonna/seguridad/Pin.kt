package com.fabri.nonna.seguridad

import java.security.MessageDigest
import java.security.SecureRandom

object Pin {
    fun valido(p: String) = p.length in 4..6 && p.all(Char::isDigit)

    fun nuevaSal(): String {
        val b = ByteArray(16); SecureRandom().nextBytes(b)
        return b.joinToString("") { "%02x".format(it) }
    }

    fun hash(pin: String, sal: String): String =
        MessageDigest.getInstance("SHA-256").digest((sal + pin).toByteArray()).joinToString("") { "%02x".format(it) }

    fun comprobar(pin: String, sal: String, hashGuardado: String) =
        hashGuardado.isNotEmpty() && hash(pin, sal) == hashGuardado
}

class IntentosPin(
    private val reloj: () -> Long,
    private val maxFallos: Int = 5,
    private val esperaMs: Long = 30_000
) {
    private var fallos = 0
    private var bloqueadoHasta = 0L

    fun bloqueado() = reloj() < bloqueadoHasta
    fun segundosRestantes(): Int = (((bloqueadoHasta - reloj()).coerceAtLeast(0) + 999) / 1000).toInt()
    fun fallo() { fallos++; if (fallos >= maxFallos) { bloqueadoHasta = reloj() + esperaMs; fallos = 0 } }
    fun acierto() { fallos = 0 }
}

class ContadorToques(
    private val necesarios: Int = 5,
    private val ventanaMs: Long = 3_000,
    private val reloj: () -> Long
) {
    private val toques = ArrayDeque<Long>()

    fun toque(): Boolean {
        val t = reloj()
        toques.addLast(t)
        while (toques.isNotEmpty() && (t - toques.first() > ventanaMs || t < toques.first())) toques.removeFirst()
        if (toques.size >= necesarios) { toques.clear(); return true }
        return false
    }
}
