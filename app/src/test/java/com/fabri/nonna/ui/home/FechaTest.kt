package com.fabri.nonna.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FechaTest {
    @Test fun domingoEnEspanol() =
        assertEquals("Domingo, 8 de noviembre", fechaLarga(LocalDate.of(2026, 11, 8)))
    @Test fun miercoles() =
        assertEquals("Miércoles, 1 de abril", fechaLarga(LocalDate.of(2026, 4, 1)))
}
