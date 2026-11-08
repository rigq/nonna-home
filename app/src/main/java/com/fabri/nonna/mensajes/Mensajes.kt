package com.fabri.nonna.mensajes

import android.app.Activity
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.ConcurrentHashMap

object Mensajes {
    val bandeja = BandejaMensajes()
    internal val accesos = ConcurrentHashMap<String, PendingIntent>()
    @Volatile var conectado = false

    fun tienePermiso(ctx: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)

    fun reconectar(ctx: Context) {
        if (tienePermiso(ctx) && !conectado) {
            NotificationListenerService.requestRebind(ComponentName(ctx, LectorNotificaciones::class.java))
        }
    }

    /** Abre el chat exacto de la notificación. false si ya no existe. */
    fun abrir(activity: Activity, clave: String): Boolean {
        val pi = accesos[clave] ?: return false
        return try {
            val opciones = if (Build.VERSION.SDK_INT >= 34) ActivityOptions.makeBasic()
                .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                .toBundle() else null
            pi.send(activity, 0, null, null, null, null, opciones)
            true
        } catch (e: Exception) { false }
    }
}
