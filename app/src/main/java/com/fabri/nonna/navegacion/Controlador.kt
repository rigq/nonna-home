package com.fabri.nonna.navegacion

import com.fabri.nonna.acciones.Accion
import com.fabri.nonna.acciones.Lanzador
import com.fabri.nonna.acciones.Resultado
import com.fabri.nonna.feedback.Antirrebote
import com.fabri.nonna.feedback.Voz
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class Controlador(
    private val lanzador: Lanzador,
    private val antirrebote: Antirrebote,
    val voz: Voz,
    private val vibrar: () -> Unit
) {
    private val _errores = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val errores: SharedFlow<Unit> = _errores.asSharedFlow()

    /** Para botones que solo navegan dentro de la app. */
    fun permitir(frase: String?): Boolean {
        if (!antirrebote.intentar()) return false
        vibrar()
        frase?.let(voz::decir)
        return true
    }

    fun pulsar(a: Accion, frase: String) {
        if (!permitir(frase)) return
        if (lanzador.ejecutar(a) == Resultado.Error) _errores.tryEmit(Unit)
    }

    fun mostrarError() { _errores.tryEmit(Unit) }

    fun nuevaPantallaDeConfirmacion() = antirrebote.reiniciar()
}
