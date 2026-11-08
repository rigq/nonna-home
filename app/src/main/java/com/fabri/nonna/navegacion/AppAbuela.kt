package com.fabri.nonna.navegacion

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.fabri.nonna.acciones.Accion
import com.fabri.nonna.acciones.WhatsAppContactos
import com.fabri.nonna.datos.Ajustes
import com.fabri.nonna.datos.Repositorio
import com.fabri.nonna.datos.TipoTarjeta
import com.fabri.nonna.ui.dia.TarjetaDiaScreen
import com.fabri.nonna.mensajes.Asociacion
import com.fabri.nonna.mensajes.MensajeNuevo
import com.fabri.nonna.mensajes.Mensajes
import com.fabri.nonna.ui.familia.SeccionMensajes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.fabri.nonna.ui.components.BotonVolver
import com.fabri.nonna.ui.error.ErrorScreen
import com.fabri.nonna.ui.familia.ConfirmarScreen
import com.fabri.nonna.ui.familia.FamiliaScreen
import com.fabri.nonna.ui.home.HomeScreen

@Composable
fun AppAbuela(controlador: Controlador, irAInicio: Int, repo: Repositorio) {
    var pantalla by remember { mutableStateOf<Pantalla>(Pantalla.Inicio) }
    val familiares by repo.familiares.collectAsState(initial = emptyList())
    val ajustes by repo.ajustes.collectAsState(initial = Ajustes())
    val mensajes by Mensajes.bandeja.lista.collectAsState()
    val ctx = LocalContext.current
    val nombresAgenda by produceState(emptyMap<String, String>(), familiares) {
        value = withContext(Dispatchers.IO) {
            familiares.mapNotNull { f -> WhatsAppContactos.nombreEnAgenda(ctx, f.telefono)?.let { f.id to it } }.toMap()
        }
    }
    val familiarDe = { m: MensajeNuevo -> Asociacion.familiarDe(m, familiares, nombresAgenda) }
    val volver = { pantalla = Pantalla.Inicio }

    LaunchedEffect(ajustes.vozActivada) { controlador.voz.activada = ajustes.vozActivada }
    LaunchedEffect(irAInicio) { pantalla = Pantalla.Inicio }
    LaunchedEffect(controlador) { controlador.errores.collect { pantalla = Pantalla.Error } }
    BackHandler(enabled = true) {
        pantalla = when (pantalla) {
            is Pantalla.Confirmar -> Pantalla.Familia
            else -> Pantalla.Inicio
        }
    }

    when (val p = pantalla) {
        Pantalla.Inicio -> HomeScreen(
            controlador = controlador, temperatura = null, onToqueReloj = {},
            onFamilia = { pantalla = Pantalla.Familia },
            onDia = {
                when {
                    ajustes.tarjetaTipo == TipoTarjeta.APP -> controlador.pulsar(Accion.TarjetaDiaApp, "Abriendo la tarjeta del Día")
                    ajustes.tarjetaNumero.isBlank() -> controlador.mostrarError()
                    else -> if (controlador.permitir("Tarjeta del Día")) pantalla = Pantalla.TarjetaDia
                }
            }
        )
        Pantalla.Error -> ErrorScreen(ajustes.nombreAyuda, volver)
        Pantalla.Familia -> FamiliaScreen(
            familiares = familiares,
            nuevosDe = { f -> Asociacion.nuevosDe(f, mensajes, familiares, nombresAgenda) },
            cabecera = {
                SeccionMensajes(mensajes, familiarDe) { m ->
                    val f = familiarDe(m)
                    controlador.pulsar(
                        Accion.AbrirMensaje(m.clave, f?.telefono),
                        "Abriendo los mensajes de ${f?.nombre ?: m.conversacion}"
                    )
                }
            },
            onVideo = { f -> if (controlador.permitir("¿Llamar a ${f.nombre}?")) pantalla = Pantalla.Confirmar(f) },
            onMensajes = { f -> controlador.pulsar(Accion.WhatsAppChat(f.telefono), "Abriendo los mensajes de ${f.nombre}") },
            onAbrirWhatsApp = { controlador.pulsar(Accion.AbrirWhatsApp, "Abriendo WhatsApp") },
            onVolver = volver
        )
        is Pantalla.Confirmar -> {
            LaunchedEffect(p) { controlador.nuevaPantallaDeConfirmacion() }
            ConfirmarScreen(
                familiar = p.familiar,
                onSi = {
                    pantalla = Pantalla.Familia
                    controlador.pulsar(Accion.WhatsAppVideo(p.familiar.telefono), "Llamando a ${p.familiar.nombre}")
                },
                onNo = { pantalla = Pantalla.Familia }
            )
        }
        Pantalla.TarjetaDia -> TarjetaDiaScreen(ajustes.tarjetaNumero, ajustes.tarjetaTipo, volver)
    }
}
