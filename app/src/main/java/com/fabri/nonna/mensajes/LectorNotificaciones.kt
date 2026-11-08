package com.fabri.nonna.mensajes

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fabri.nonna.BuildConfig
import com.fabri.nonna.acciones.Paquetes

class LectorNotificaciones : NotificationListenerService() {

    override fun onListenerConnected() {
        Mensajes.conectado = true
        Mensajes.bandeja.vaciar(); Mensajes.accesos.clear()
        activeNotifications?.forEach { onNotificationPosted(it) }
    }

    override fun onListenerDisconnected() { Mensajes.conectado = false }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != Paquetes.WHATSAPP) return
        val cruda = runCatching { aCruda(sbn) }.getOrNull() ?: return
        if (BuildConfig.DEBUG) Log.i("SONDA_NOTIF", cruda.toString())
        Mensajes.bandeja.publicar(cruda)
        sbn.notification.contentIntent?.let { Mensajes.accesos[sbn.key] = it }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName != Paquetes.WHATSAPP) return
        Mensajes.bandeja.quitar(sbn.key); Mensajes.accesos.remove(sbn.key)
    }

    private fun aCruda(sbn: StatusBarNotification): NotifCruda {
        val n = sbn.notification
        val estilo = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(n)
        val extras = n.extras
        return NotifCruda(
            clave = sbn.key,
            esResumen = (n.flags and Notification.FLAG_GROUP_SUMMARY) != 0,
            categoria = n.category,
            conversacion = estilo?.conversationTitle?.toString(),
            esGrupo = estilo?.isGroupConversation ?: false,
            mensajes = estilo?.messages?.map { (it.person?.name?.toString().orEmpty()) to (it.text?.toString().orEmpty()) }.orEmpty(),
            titulo = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            texto = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            hora = sbn.postTime
        )
    }
}
