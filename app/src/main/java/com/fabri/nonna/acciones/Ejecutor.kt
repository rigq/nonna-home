package com.fabri.nonna.acciones

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Process
import android.provider.MediaStore
import android.util.Log
import com.fabri.nonna.BuildConfig

class Ejecutor(private val activity: Activity) {

    fun abrir(d: Destino): Boolean = intentar(d).also { if (BuildConfig.DEBUG) Log.i(TAG, "$d -> $it") }

    private fun intentar(d: Destino): Boolean = when (d) {
        is Destino.Ver -> lanzar(Intent(Intent.ACTION_VIEW).apply {
            if (d.mime != null) setDataAndType(Uri.parse(d.uri), d.mime) else data = Uri.parse(d.uri)
            d.paquete?.let { setPackage(it) }
        })
        is Destino.AccionSistema -> lanzar(Intent(d.action))
        Destino.CamaraPorDefecto -> {
            val paquete = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .resolveActivity(activity.packageManager)?.packageName
            paquete?.let { abrir(Destino.AbrirApp(it)) } ?: false
        }
        is Destino.AbrirApp ->
            activity.packageManager.getLaunchIntentForPackage(d.paquete)?.let { lanzar(it) } ?: false
        is Destino.Atajo -> lanzarAtajo(d)
        is Destino.Notificacion -> com.fabri.nonna.mensajes.Mensajes.abrir(activity, d.clave)
        is Destino.VideoWhatsApp ->
            WhatsAppContactos.buscarVideollamada(activity, d.telefono)?.let { uri ->
                lanzar(Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, WhatsAppContactos.MIME_VIDEO)
                    setPackage(Paquetes.WHATSAPP)
                })
            } ?: false
    }

    private fun lanzar(i: Intent): Boolean = try {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(i)
        true
    } catch (e: ActivityNotFoundException) { fallo(e) } catch (e: SecurityException) { fallo(e) }

    private fun fallo(e: Exception): Boolean { if (BuildConfig.DEBUG) Log.w(TAG, "fallo al abrir", e); return false }

    private companion object { const val TAG = "NONNA_ACCION" }

    private fun lanzarAtajo(d: Destino.Atajo): Boolean {
        val la = activity.getSystemService(LauncherApps::class.java) ?: return false
        if (!la.hasShortcutHostPermission()) return false
        val q = LauncherApps.ShortcutQuery().setPackage(d.paquete).setQueryFlags(
            LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
        )
        val lista = try { la.getShortcuts(q, Process.myUserHandle()) } catch (e: Exception) { null } ?: return false
        val atajo = lista.firstOrNull { s ->
            val t = "${s.id} ${s.shortLabel} ${s.longLabel}".lowercase()
            d.palabras.any { it in t }
        } ?: return false
        return try { la.startShortcut(atajo, null, null); true } catch (e: Exception) { false }
    }
}
