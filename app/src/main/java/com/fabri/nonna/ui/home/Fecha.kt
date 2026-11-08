package com.fabri.nonna.ui.home

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ES = Locale.forLanguageTag("es-ES")
private val FORMATO = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", ES)

fun fechaLarga(d: LocalDate): String = d.format(FORMATO).replaceFirstChar { it.titlecase(ES) }
