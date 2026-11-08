package com.fabri.nonna.navegacion

sealed interface Pantalla {
    data object Inicio : Pantalla
    data object Familia : Pantalla
    data object TarjetaDia : Pantalla
    data object Error : Pantalla
}
