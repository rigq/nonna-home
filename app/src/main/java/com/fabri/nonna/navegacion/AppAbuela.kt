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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.fabri.nonna.acciones.Accion
import com.fabri.nonna.datos.Ajustes
import com.fabri.nonna.datos.Repositorio
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
            onDia = { if (controlador.permitir("Tarjeta del Día")) pantalla = Pantalla.TarjetaDia }
        )
        Pantalla.Error -> ErrorScreen(ajustes.nombreAyuda, volver)
        Pantalla.Familia -> FamiliaScreen(
            familiares = familiares,
            nuevosDe = { 0 },
            cabecera = {},
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
        Pantalla.TarjetaDia -> Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Text("En construcción", fontSize = 32.sp, modifier = Modifier.weight(1f))
            BotonVolver(volver)
        }
    }
}
