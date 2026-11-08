package com.fabri.nonna.mensajes

import android.app.Activity
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.ConcurrentHashMap

object Mensajes {
    val bandeja = BandejaMensajes()
    internal val accesos = ConcurrentHashMap<String, PendingIntent>()
    @Volatile var conectado = false

    fun tienePermiso(ctx: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)

    /**
     * Tras actualizar la app (o si MIUI la mata), Android deja el lector desconectado aunque el
     * acceso siga activo, y en el Redmi requestRebind no basta. Si a los 3 s sigue sin conectar,
     * se desactiva y reactiva el propio componente, lo que obliga al sistema a volver a enlazarlo.
     */
    fun reconectar(ctx: Context) {
        if (!tienePermiso(ctx) || conectado) return
        val app = ctx.applicationContext
        val componente = ComponentName(app, LectorNotificaciones::class.java)
        runCatching { NotificationListenerService.requestRebind(componente) }
        Handler(Looper.getMainLooper()).postDelayed({
            if (!conectado && tienePermiso(app)) reiniciarComponente(app, componente)
        }, 3_000)
    }

    private fun reiniciarComponente(ctx: Context, c: ComponentName) = runCatching {
        val pm = ctx.packageManager
        pm.setComponentEnabledSetting(c, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        pm.setComponentEnabledSetting(c, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
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
