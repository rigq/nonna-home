package com.fabri.nonna.navegacion

import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.fabri.nonna.acciones.Accion
import com.fabri.nonna.acciones.WhatsAppContactos
import com.fabri.nonna.admin.Comprobacion
import com.fabri.nonna.admin.Diagnostico
import com.fabri.nonna.datos.Ajustes
import com.fabri.nonna.datos.Familiar
import com.fabri.nonna.datos.Repositorio
import com.fabri.nonna.datos.TipoTarjeta
import com.fabri.nonna.mensajes.Asociacion
import com.fabri.nonna.mensajes.MensajeNuevo
import com.fabri.nonna.mensajes.Mensajes
import com.fabri.nonna.seguridad.ContadorToques
import com.fabri.nonna.seguridad.IntentosPin
import com.fabri.nonna.seguridad.Pin
import com.fabri.nonna.tiempo.OpenMeteo
import com.fabri.nonna.tiempo.ServicioTiempo
import com.fabri.nonna.ui.admin.AdminFamiliaresScreen
import com.fabri.nonna.ui.admin.AdminScreen
import com.fabri.nonna.ui.admin.AjustesAdminScreen
import com.fabri.nonna.ui.admin.AsistenteScreen
import com.fabri.nonna.ui.admin.DiagnosticoScreen
import com.fabri.nonna.ui.admin.EditarFamiliarScreen
import com.fabri.nonna.ui.admin.PinScreen
import com.fabri.nonna.ui.dia.TarjetaDiaScreen
import com.fabri.nonna.ui.error.ErrorScreen
import com.fabri.nonna.ui.familia.ConfirmarScreen
import com.fabri.nonna.ui.familia.FamiliaScreen
import com.fabri.nonna.ui.familia.SeccionMensajes
import com.fabri.nonna.ui.home.HomeScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun AppAbuela(controlador: Controlador, irAInicio: Int, repo: Repositorio) {
    var pantalla by remember { mutableStateOf<Pantalla>(Pantalla.Inicio) }
    val familiares by repo.familiares.collectAsState(initial = emptyList())
    val ajustesCargados by remember(repo) { repo.ajustes.map<Ajustes, Ajustes?> { it } }.collectAsState(initial = null)
    val ajustes = ajustesCargados ?: Ajustes()
    val mensajes by Mensajes.bandeja.lista.collectAsState()
    val tiempo by repo.tiempo.collectAsState(initial = null)
    val tiempoActual by rememberUpdatedState(tiempo)
    val ajustesActuales by rememberUpdatedState(ajustes)
    val servicioTiempo = remember(repo) { ServicioTiempo(repo) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val intentos = remember { IntentosPin({ SystemClock.elapsedRealtime() }) }
    val toquesReloj = remember { ContadorToques(reloj = { SystemClock.elapsedRealtime() }) }
    val nombresAgenda by produceState(emptyMap<String, String>(), familiares) {
        value = withContext(Dispatchers.IO) {
            familiares.mapNotNull { f -> WhatsAppContactos.nombreEnAgenda(ctx, f.telefono)?.let { f.id to it } }.toMap()
        }
    }
    val familiarDe = { m: MensajeNuevo -> Asociacion.familiarDe(m, familiares, nombresAgenda) }
    val inicioOAsistente = { if (ajustes.configurado) Pantalla.Inicio else Pantalla.Asistente }
    val volver = { pantalla = Pantalla.Inicio }
    val alMenuFamiliares = { pantalla = if (ajustes.configurado) Pantalla.AdminFamiliares else Pantalla.Asistente }

    LaunchedEffect(ajustes.vozActivada) { controlador.voz.activada = ajustes.vozActivada }
    LaunchedEffect(ajustesCargados?.configurado) {
        if (ajustesCargados?.configurado == false && pantalla !is Pantalla.EditarFamiliar) pantalla = Pantalla.Asistente
    }
    LaunchedEffect(irAInicio) { if (ajustes.configurado) pantalla = Pantalla.Inicio }
    LaunchedEffect(controlador) { controlador.errores.collect { pantalla = Pantalla.Error } }
    BackHandler(enabled = true) {
        pantalla = when (pantalla) {
            is Pantalla.Confirmar -> Pantalla.Familia
            Pantalla.AdminFamiliares, Pantalla.AjustesAdmin, Pantalla.Diagnostico -> Pantalla.Admin
            is Pantalla.EditarFamiliar -> if (ajustes.configurado) Pantalla.AdminFamiliares else Pantalla.Asistente
            else -> inicioOAsistente()
        }
    }

    when (val p = pantalla) {
        Pantalla.Inicio -> {
            LaunchedEffect(ajustes.lat, ajustes.lon) {
                while (true) {
                    servicioTiempo.refrescarSiToca(ajustesActuales, tiempoActual)
                    delay(60_000)
                }
            }
            HomeScreen(
                controlador = controlador,
                temperatura = tiempo?.let { OpenMeteo.texto(it) },
                onToqueReloj = { if (toquesReloj.toque()) pantalla = Pantalla.Pin },
                onFamilia = { pantalla = Pantalla.Familia },
                onDia = {
                    when {
                        ajustes.tarjetaTipo == TipoTarjeta.APP -> controlador.pulsar(Accion.TarjetaDiaApp, "Abriendo la tarjeta del Día")
                        ajustes.tarjetaNumero.isBlank() -> controlador.mostrarError()
                        else -> if (controlador.permitir("Tarjeta del Día")) pantalla = Pantalla.TarjetaDia
                    }
                }
            )
        }
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

        // ---------- Modo administrador ----------
        Pantalla.Pin -> PinScreen(ajustes, intentos, onOk = { pantalla = Pantalla.Admin }, onCancelar = volver)
        Pantalla.Admin -> AdminScreen(
            onFamiliares = { pantalla = Pantalla.AdminFamiliares },
            onAjustes = { pantalla = Pantalla.AjustesAdmin },
            onDiagnostico = { pantalla = Pantalla.Diagnostico },
            onSalirLauncher = {
                runCatching {
                    ctx.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            },
            onVolver = volver
        )
        Pantalla.AdminFamiliares -> AdminFamiliaresScreen(
            familiares,
            onEditar = { pantalla = Pantalla.EditarFamiliar(it) },
            onGuardarLista = { l -> scope.launch { repo.guardarFamiliares(l) } },
            onVolver = { pantalla = Pantalla.Admin }
        )
        is Pantalla.EditarFamiliar -> EditarFamiliarScreen(
            p.familiar,
            onGuardar = { nombre, tel, foto ->
                scope.launch {
                    val id = p.familiar?.id ?: UUID.randomUUID().toString()
                    val ruta = foto?.let { repo.copiarFoto(it, id, p.familiar?.rutaFoto) } ?: p.familiar?.rutaFoto
                    val nuevo = Familiar(id, nombre, tel, ruta, p.familiar?.orden ?: familiares.size)
                    repo.guardarFamiliares(
                        if (p.familiar == null) familiares + nuevo else familiares.map { if (it.id == id) nuevo else it }
                    )
                    alMenuFamiliares()
                }
            },
            onCancelar = alMenuFamiliares
        )
        Pantalla.AjustesAdmin -> AjustesAdminScreen(
            ajustes,
            onGuardar = { nuevos -> scope.launch { repo.guardarAjustes(nuevos); pantalla = Pantalla.Admin } },
            onVolver = { pantalla = Pantalla.Admin }
        )
        Pantalla.Diagnostico -> {
            var vuelta by remember { mutableStateOf(0) }
            val lista by produceState(emptyList<Comprobacion>(), ajustes, familiares, tiempo, vuelta) {
                value = withContext(Dispatchers.IO) { Diagnostico.comprobar(ctx, ajustes, familiares, tiempo) }
            }
            DiagnosticoScreen(lista, controlador, familiares, onPermisos = { vuelta++ }) { pantalla = Pantalla.Admin }
        }
        Pantalla.Asistente -> AsistenteScreen(
            yaHayPin = ajustes.pinHash.isNotEmpty(),
            hayFamiliares = familiares.isNotEmpty(),
            onPinCreado = { pin ->
                scope.launch {
                    val sal = Pin.nuevaSal()
                    repo.guardarAjustes(ajustesActuales.copy(pinSal = sal, pinHash = Pin.hash(pin, sal)))
                }
            },
            onAnadirFamiliar = { pantalla = Pantalla.EditarFamiliar(null) },
            onTerminar = { scope.launch { repo.guardarAjustes(ajustesActuales.copy(configurado = true)); pantalla = Pantalla.Inicio } }
        )
    }
}
