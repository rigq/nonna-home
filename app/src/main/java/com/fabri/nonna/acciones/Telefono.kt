package com.fabri.nonna.acciones

object Telefono {
    /** Solo dígitos con prefijo de país. "" si no hay número. */
    fun normalizar(t: String, prefijo: String = "34"): String {
        val limpio = t.filter { it.isDigit() || it == '+' }
        val sinPrefijoInternacional = when {
            limpio.startsWith("+") -> limpio.drop(1)
            limpio.startsWith("00") -> limpio.drop(2)
            else -> limpio
        }.filter(Char::isDigit)
        if (sinPrefijoInternacional.isEmpty()) return ""
        val esNacional = !limpio.startsWith("+") && !limpio.startsWith("00") && sinPrefijoInternacional.length == 9
        return if (esNacional) prefijo + sinPrefijoInternacional else sinPrefijoInternacional
    }

    fun mismo(a: String, b: String): Boolean {
        val na = normalizar(a)
        return na.isNotEmpty() && na == normalizar(b)
    }

    fun paraWaMe(t: String): String = normalizar(t)
}
