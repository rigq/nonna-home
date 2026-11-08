package com.fabri.nonna.admin

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
        val modoVoz = Intent(Intent.ACTION_VIEW, Uri.parse(Lanzador.URL_VOZ_CHATGPT))
            .setPackage(Paquetes.CHATGPT).resolveActivity(pm) != null

        val lista = mutableListOf(
            Comprobacion("Pantalla de inicio predeterminada", esInicio, "Ajustes → Apps → Apps predeterminadas → Pantalla de inicio"),
            Comprobacion("WhatsApp instalado", instalada(Paquetes.WHATSAPP), "Instala WhatsApp"),
            Comprobacion("YouTube instalado", instalada(Paquetes.YOUTUBE), "Instala YouTube"),
            Comprobacion("ChatGPT instalado", instalada(Paquetes.CHATGPT), "Instala ChatGPT e inicia sesión"),
            Comprobacion("ChatGPT abre en modo voz", modoVoz, "Actualiza ChatGPT; si no, se abrirá normal (plan B)"),
            Comprobacion("Permiso de contactos", WhatsAppContactos.tienePermiso(ctx), "Ajustes → Apps → Inicio Nonna → Permisos"),
            Comprobacion("Permiso de llamadas", WhatsAppContactos.tienePermisoLlamar(ctx), "Ajustes → Apps → Inicio Nonna → Permisos → Teléfono → Permitir"),
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
