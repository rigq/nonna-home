package com.fabri.nonna.admin

import android.app.role.RoleManager
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import com.fabri.nonna.acciones.Lanzador
import com.fabri.nonna.acciones.Paquetes
import com.fabri.nonna.acciones.WhatsAppContactos
import com.fabri.nonna.datos.Ajustes
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.datos.TiempoCache
import com.fabri.nonna.datos.TipoTarjeta
import com.fabri.nonna.mensajes.Mensajes

data class Comprobacion(val nombre: String, val ok: Boolean, val ayuda: String)

object Diagnostico {
    fun comprobar(ctx: Context, ajustes: Ajustes, familiares: List<Familiar>, tiempo: TiempoCache?): List<Comprobacion> {
        val pm = ctx.packageManager
        fun instalada(p: String) = pm.getLaunchIntentForPackage(p) != null
        val esInicio = ctx.getSystemService(RoleManager::class.java)?.isRoleHeld(RoleManager.ROLE_HOME) == true
        val atajoVoz = runCatching {
            val la = ctx.getSystemService(LauncherApps::class.java)
            val q = LauncherApps.ShortcutQuery().setPackage(Paquetes.CHATGPT).setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
            )
            la.getShortcuts(q, Process.myUserHandle()).orEmpty().any { s ->
                val t = "${s.id} ${s.shortLabel} ${s.longLabel}".lowercase(); Lanzador.PALABRAS_VOZ.any { it in t }
            }
        }.getOrDefault(false)

        val lista = mutableListOf(
            Comprobacion("Pantalla de inicio predeterminada", esInicio, "Ajustes → Apps → Apps predeterminadas → Pantalla de inicio"),
            Comprobacion("WhatsApp instalado", instalada(Paquetes.WHATSAPP), "Instala WhatsApp"),
            Comprobacion("YouTube instalado", instalada(Paquetes.YOUTUBE), "Instala YouTube"),
            Comprobacion("ChatGPT instalado", instalada(Paquetes.CHATGPT), "Instala ChatGPT e inicia sesión"),
            Comprobacion("Atajo de voz de ChatGPT", atajoVoz, "Se abrirá ChatGPT normal (plan B)"),
            Comprobacion("Permiso de contactos", WhatsAppContactos.tienePermiso(ctx), "Ajustes → Apps → Inicio Nonna → Permisos"),
            Comprobacion("Acceso a notificaciones", Mensajes.tienePermiso(ctx), "Ajustes → Acceso a notificaciones → Inicio Nonna - mensajes"),
            Comprobacion("Lector de mensajes conectado", Mensajes.conectado, "Activa Inicio automático y batería sin restricciones"),
            Comprobacion(
                "Tarjeta DIA configurada",
                if (ajustes.tarjetaTipo == TipoTarjeta.APP) instalada(Paquetes.DIA) else ajustes.tarjetaNumero.isNotBlank(),
                "Ajustes → Tarjeta DIA"
            ),
            Comprobacion("Ciudad configurada", ajustes.lat != null, "Ajustes → Buscar ciudad"),
            Comprobacion("Temperatura recibida", tiempo != null, "Revisa la conexión a internet")
        )
        familiares.forEach { f ->
            lista += Comprobacion(
                "Videollamada directa: ${f.nombre}",
                WhatsAppContactos.buscarVideollamada(ctx, f.telefono) != null,
                "Guarda ${f.telefono} en Contactos y abre WhatsApp para que sincronice"
            )
        }
        return lista
    }
}
