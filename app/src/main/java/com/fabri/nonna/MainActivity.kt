package com.fabri.nonna

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableIntStateOf
import com.fabri.nonna.acciones.Ejecutor
import com.fabri.nonna.acciones.Lanzador
import com.fabri.nonna.datos.Repositorio
import com.fabri.nonna.feedback.Antirrebote
import com.fabri.nonna.feedback.Voz
import com.fabri.nonna.feedback.vibrarCorto
import com.fabri.nonna.navegacion.AppAbuela
import com.fabri.nonna.navegacion.Controlador
import com.fabri.nonna.ui.theme.TemaAbuela

class MainActivity : ComponentActivity() {
    private val irAInicio = mutableIntStateOf(0)
    private lateinit var voz: Voz
    private lateinit var controlador: Controlador
    private lateinit var repo: Repositorio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voz = Voz(this)
        repo = Repositorio(this)
        val ejecutor = Ejecutor(this)
        controlador = Controlador(Lanzador(ejecutor::abrir), Antirrebote(), voz) { vibrarCorto(this) }
        setContent {
            TemaAbuela { AppAbuela(controlador, irAInicio.intValue, repo) }
        }
    }

    /** Pulsar ○ estando ya en el launcher → volver a la pantalla de inicio. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        irAInicio.intValue++
    }

    override fun onResume() {
        super.onResume()
        com.fabri.nonna.mensajes.Mensajes.reconectar(this)
    }

    override fun onDestroy(){ voz.cerrar(); super.onDestroy() }
}
