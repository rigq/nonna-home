package com.fabri.nonna.navegacion

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.fabri.nonna.ui.components.BotonVolver
import com.fabri.nonna.ui.error.ErrorScreen
import com.fabri.nonna.ui.home.HomeScreen

@Composable
fun AppAbuela(controlador: Controlador, irAInicio: Int) {
    var pantalla by remember { mutableStateOf<Pantalla>(Pantalla.Inicio) }
    val volver = { pantalla = Pantalla.Inicio }

    LaunchedEffect(irAInicio) { pantalla = Pantalla.Inicio }
    LaunchedEffect(controlador) { controlador.errores.collect { pantalla = Pantalla.Error } }
    BackHandler(enabled = true) { if (pantalla != Pantalla.Inicio) volver() }

    when (pantalla) {
        Pantalla.Inicio -> HomeScreen(
            controlador = controlador, temperatura = null, onToqueReloj = {},
            onFamilia = { pantalla = Pantalla.Familia },
            onDia = { if (controlador.permitir("Tarjeta del Día")) pantalla = Pantalla.TarjetaDia }
        )
        Pantalla.Error -> ErrorScreen("Fabri", volver)
        Pantalla.Familia, Pantalla.TarjetaDia -> Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Text("En construcción", fontSize = 32.sp, modifier = Modifier.weight(1f))
            BotonVolver(volver)
        }
    }
}
