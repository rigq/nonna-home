package com.fabri.nonna.mensajes

import android.app.ActivityOptions
import android.app.Notification
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.Log
import com.fabri.nonna.BuildConfig
import java.util.concurrent.ConcurrentHashMap

/**
 * Android solo muestra la pantalla completa de una llamada entrante si nadie está usando el móvil.
 * Como Inicio Nonna siempre está delante, las llamadas salían como notificación flotante.
 * Si llega una llamada entrante con pantalla completa y nuestra app está en pantalla, la abrimos nosotros.
 */
object LlamadaEntrante {
    const val TIPO_DESCONOCIDO = 0          // Notification.CALL_TYPE_UNKNOWN
    const val TIPO_ENTRANTE = 1             // Notification.CALL_TYPE_INCOMING
    const val TIPO_EN_CURSO = 2             // Notification.CALL_TYPE_ONGOING
    const val TIPO_FILTRADA = 3             // Notification.CALL_TYPE_SCREENING

    /** Lo pone MainActivity en onResume/onPause: solo abrimos la llamada si ella está en nuestra pantalla. */
    @Volatile var appEnPantalla = false

    private val abiertas = ConcurrentHashMap.newKeySet<String>()

    fun esEntrante(categoria: String?, tienePantallaCompleta: Boolean, tipo: Int): Boolean =
        categoria == Notification.CATEGORY_CALL && tienePantallaCompleta &&
            (tipo == TIPO_ENTRANTE || tipo == TIPO_DESCONOCIDO)

    fun alPublicar(sbn: StatusBarNotification) {
        val n = sbn.notification
        val fsi = n.fullScreenIntent
        val tipo = n.extras.getInt(Notification.EXTRA_CALL_TYPE, TIPO_DESCONOCIDO)
        if (!esEntrante(n.category, fsi != null, tipo)) return
        // La notificación se actualiza varias veces mientras suena: abrirla solo una vez.
        if (!appEnPantalla || !abiertas.add(sbn.key)) return
        val opciones = if (Build.VERSION.SDK_INT >= 34) ActivityOptions.makeBasic()
            .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
            .toBundle() else null
        val ok = runCatching { fsi!!.send(null, 0, null, null, null, null, opciones) }
        if (BuildConfig.DEBUG) Log.i("NONNA_LLAMADA", "${sbn.packageName} pantalla completa -> ${ok.isSuccess} ${ok.exceptionOrNull() ?: ""}")
    }

    fun alQuitar(sbn: StatusBarNotification) { abiertas.remove(sbn.key) }
}
