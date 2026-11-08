package com.fabri.nonna.navegacion

import com.fabri.nonna.datos.Familiar

sealed interface Pantalla {
    data object Inicio : Pantalla
    data object Familia : Pantalla
    data object TarjetaDia : Pantalla
    data object Error : Pantalla
    data class Confirmar(val familiar: Familiar) : Pantalla
}
