# Pastillas sin olvidos — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Recordar a la Nonna sus pastillas de la mañana (08:50) y de la noche (20:50) desde Inicio Nonna, dejarle marcarlas con dos toques, y avisar a la familia por ntfy si no las marca.

**Architecture:** Lógica pura y testeable (`Pastillas`, `Ntfy`) que decide qué está pendiente y cuándo ocurre el siguiente evento; una única alarma `setAlarmClock` programa ese evento y un `BroadcastReceiver` lo atiende (notificación + voz, o aviso ntfy) y reprograma el siguiente. La UI de la Nonna (franja en Inicio + pantalla de confirmación) y la de admin (sección en Ajustes + comprobaciones) leen el mismo DataStore.

**Tech Stack:** Kotlin 2.2 (AGP 9 integrado), Jetpack Compose Material3, DataStore Preferences, kotlinx.serialization, `java.time`, AlarmManager, NotificationCompat, TextToSpeech, HttpURLConnection, JUnit4.

**Spec:** `docs/superpowers/specs/2026-11-09-pastillas-design.md`

## Global Constraints

- Paquete `com.fabri.nonna`; minSdk 29, targetSdk/compileSdk 37; probado en Redmi Android 13 (MIUI 14).
- Horas por defecto: mañana `08:50` (530 min), noche `20:50` (1250 min); espera antes de avisar: 60 min mañana, 120 min noche.
- Textos de la Nonna: "Pastillas de la mañana" / "Pastillas de la noche"; botones "SÍ, YA ME LAS TOMÉ" y "TODAVÍA NO".
- Voz de la mañana: "Buenos días, Nonna. Toca la pastilla de la mañana." Al marcar: "¡Muy bien, Nonna!". **Nunca voz por la noche.**
- Un solo aviso por toma; sin repeticiones ni alarmas insistentes.
- Aviso a la familia solo por ntfy (`https://ntfy.sh`, publicación JSON). Canal: `nonna-` + 16 hex. Reintentos: 3, cada 10 min.
- Todo el código nuevo y los textos en español, siguiendo el estilo del proyecto (nombres en español).
- Logs solo con `BuildConfig.DEBUG`.
- Comandos de test/instalación desde la raíz del proyecto en Git Bash:
  `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"` y luego `./gradlew …`.

## Review Focus

1. **Reinicio del móvil o actualización de la app** — la alarma se pierde; debe reprogramarse en `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, al abrir la app y al guardar Ajustes (Task 4 lo cablea; Task 7 lo verifica con `dumpsys alarm` tras reiniciar).
2. **Marcar la toma de la noche pasada la medianoche** (vuelve de caminar a las 00:30) — debe marcar la de *ayer*, no crear una pendiente nueva (test en Task 1: `nochePasadaMedianocheSigueSiendoLaDeAyer` y `marcarDeMadrugadaGuardaLaFechaDeAyer`).
3. **Día en que se activa la función** — a las 07:00 del primer día no debe aparecer "pastillas de la noche" de ayer ni avisar por ella (tests en Task 1: `antesDeActivarNoHayPendiente`, `noAvisaDeTomasAnterioresALaActivacion`).
4. **Alarma que se dispara unos milisegundos antes** — no debe volver a programar el mismo evento y avisar dos veces (Task 4 reprograma desde `maxOf(ahora, evento.cuando)`; test en Task 1: `siguienteDesdeElPropioEventoDevuelveElPosterior`).
5. **Horas inválidas o al revés en Ajustes** (`25:00`, `abc`, mañana 21:00 > noche 20:50) — se rechazan y Guardar queda desactivado (tests en Task 1: `parsearHora…`, `horarioAlRevesNoEsValido`; UI en Task 6).

---

## File Structure

| Archivo | Responsabilidad |
|---|---|
| `app/src/main/java/com/fabri/nonna/pastillas/Pastillas.kt` (crear) | Modelos (`Toma`, `Horario`, `TomasHechas`, `Pendiente`, `Evento`) y lógica pura |
| `app/src/main/java/com/fabri/nonna/pastillas/Ntfy.kt` (crear) | Canal, construcción del aviso y envío HTTP |
| `app/src/main/java/com/fabri/nonna/pastillas/AjustesPastillas.kt` (crear) | Extensiones `Ajustes.horario()` y `Ajustes.desde()` |
| `app/src/main/java/com/fabri/nonna/pastillas/ProgramadorPastillas.kt` (crear) | Programar/cancelar la alarma, reintentos, disparo manual |
| `app/src/main/java/com/fabri/nonna/pastillas/ReceptorPastillas.kt` (crear) | Atender eventos y broadcasts del sistema |
| `app/src/main/java/com/fabri/nonna/pastillas/AvisosPastillas.kt` (crear) | Canales y notificaciones |
| `app/src/main/java/com/fabri/nonna/pastillas/VozBreve.kt` (crear) | TTS de un solo uso desde el receptor |
| `app/src/main/res/drawable/ic_pastilla.xml` (crear) | Icono pequeño de la notificación |
| `app/src/main/java/com/fabri/nonna/datos/Modelos.kt` (modificar) | Campos nuevos en `Ajustes` |
| `app/src/main/java/com/fabri/nonna/datos/Codec.kt` (modificar) | `tomas()` / `texto(TomasHechas)` |
| `app/src/main/java/com/fabri/nonna/datos/Repositorio.kt` (modificar) | Flujo `tomas` y `marcarToma()` |
| `app/src/main/AndroidManifest.xml` (modificar) | Permisos y receptor |
| `app/src/main/java/com/fabri/nonna/MainActivity.kt` (modificar) | Reprogramar al volver a la app |
| `app/src/main/java/com/fabri/nonna/ui/home/FranjaPastilla.kt` (crear) | Franja grande en Inicio |
| `app/src/main/java/com/fabri/nonna/ui/pastillas/PastillaScreen.kt` (crear) | "¿Te has tomado…?" SÍ / TODAVÍA NO |
| `app/src/main/java/com/fabri/nonna/ui/home/HomeScreen.kt` (modificar) | Mostrar la franja |
| `app/src/main/java/com/fabri/nonna/navegacion/Pantalla.kt` (modificar) | `Pantalla.Pastilla` |
| `app/src/main/java/com/fabri/nonna/navegacion/AppAbuela.kt` (modificar) | Cableado de estado, navegación y reprogramación |
| `app/src/main/java/com/fabri/nonna/ui/admin/SeccionPastillas.kt` (crear) | Sección "Pastillas" de Ajustes |
| `app/src/main/java/com/fabri/nonna/ui/admin/AjustesAdminScreen.kt` (modificar) | Incluir la sección y validar |
| `app/src/main/java/com/fabri/nonna/admin/Diagnostico.kt` (modificar) | Comprobaciones de pastillas |
| `app/src/main/java/com/fabri/nonna/ui/admin/DiagnosticoScreen.kt` (modificar) | Acciones "Simular…" |
| `app/src/test/java/com/fabri/nonna/pastillas/PastillasTest.kt` (crear) | Tests de la lógica |
| `app/src/test/java/com/fabri/nonna/pastillas/NtfyTest.kt` (crear) | Tests de ntfy |
| `app/src/test/java/com/fabri/nonna/datos/CodecTest.kt` (modificar) | Tests de compatibilidad |

---

### Task 1: Lógica pura de las pastillas

**Files:**
- Create: `app/src/main/java/com/fabri/nonna/pastillas/Pastillas.kt`
- Test: `app/src/test/java/com/fabri/nonna/pastillas/PastillasTest.kt`

**Interfaces:**
- Consumes: nada.
- Produces:
  - `enum class Toma { MANANA, NOCHE }`, `enum class TipoEvento { RECORDAR, COMPROBAR }`
  - `data class Horario(minManana: Int = 530, minNoche: Int = 1250, esperaManana: Int = 60, esperaNoche: Int = 120)` con `val valido: Boolean`
  - `@Serializable data class TomasHechas(manana: String? = null, noche: String? = null)` (fechas ISO `yyyy-MM-dd`)
  - `data class Pendiente(toma: Toma, fecha: LocalDate)`
  - `data class Evento(tipo: TipoEvento, toma: Toma, fecha: LocalDate, cuando: LocalDateTime)`
  - `object Pastillas` con `hecha(h, toma, fecha): Boolean`, `pendiente(ahora, horario, hechas, desde: LocalDate?): Pendiente?`, `marcar(h, p): TomasHechas`, `eventosDe(fecha, horario): List<Evento>`, `siguiente(ahora, horario): Evento`, `horaProgramada(ev, horario): LocalDateTime`, `debeAvisar(ev, hechas, desde): Boolean`, `parsearHora(s): Int?`, `formatearHora(min): String`, `nombre(toma): String`, `aMillis(ldt, zona): Long`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.fabri.nonna.pastillas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class PastillasTest {
    private val h = Horario()
    private val hoy = LocalDate.of(2026, 11, 10)
    private fun a(hh: Int, mm: Int, dia: LocalDate = hoy) = dia.atTime(hh, mm)

    // ---------- pendiente ----------
    @Test fun antesDeLaMananaEstaPendienteLaNocheDeAyer() =
        assertEquals(Pendiente(Toma.NOCHE, hoy.minusDays(1)), Pastillas.pendiente(a(7, 0), h, TomasHechas(), null))

    @Test fun aLas850EstaPendienteLaManana() =
        assertEquals(Pendiente(Toma.MANANA, hoy), Pastillas.pendiente(a(8, 50), h, TomasHechas(), null))

    @Test fun mananaMarcadaNoEstaPendiente() =
        assertNull(Pastillas.pendiente(a(12, 0), h, TomasHechas(manana = hoy.toString()), null))

    @Test fun aLas2050PasaALaNoche() =
        assertEquals(Pendiente(Toma.NOCHE, hoy), Pastillas.pendiente(a(20, 50), h, TomasHechas(manana = hoy.toString()), null))

    @Test fun nochePasadaMedianocheSigueSiendoLaDeAyer() =
        assertEquals(Pendiente(Toma.NOCHE, hoy), Pastillas.pendiente(a(0, 30, hoy.plusDays(1)), h, TomasHechas(), null))

    @Test fun antesDeActivarNoHayPendiente() =
        assertNull(Pastillas.pendiente(a(7, 0), h, TomasHechas(), desde = hoy))

    // ---------- marcar ----------
    @Test fun marcarGuardaLaFechaDeLaToma() =
        assertEquals(TomasHechas(manana = hoy.toString()), Pastillas.marcar(TomasHechas(), Pendiente(Toma.MANANA, hoy)))

    @Test fun marcarDeMadrugadaGuardaLaFechaDeAyer() {
        val p = Pastillas.pendiente(a(0, 30, hoy.plusDays(1)), h, TomasHechas(), null)!!
        assertEquals(TomasHechas(noche = hoy.toString()), Pastillas.marcar(TomasHechas(), p))
    }

    // ---------- eventos ----------
    @Test fun cuatroEventosAlDia() {
        val e = Pastillas.eventosDe(hoy, h)
        assertEquals(listOf(a(8, 50), a(9, 50), a(20, 50), a(22, 50)), e.map { it.cuando })
        assertEquals(listOf(TipoEvento.RECORDAR, TipoEvento.COMPROBAR, TipoEvento.RECORDAR, TipoEvento.COMPROBAR), e.map { it.tipo })
    }

    @Test fun siguienteDespuesDeLaNocheEsLaComprobacion() =
        assertEquals(Evento(TipoEvento.COMPROBAR, Toma.NOCHE, hoy, a(22, 50)), Pastillas.siguiente(a(21, 0), h))

    @Test fun siguienteDeMadrugadaEsLaMananaDeHoy() =
        assertEquals(Evento(TipoEvento.RECORDAR, Toma.MANANA, hoy, a(8, 50)), Pastillas.siguiente(a(3, 0), h))

    @Test fun siguienteCruzaLaMedianoche() =
        assertEquals(Evento(TipoEvento.RECORDAR, Toma.MANANA, hoy.plusDays(1), a(8, 50, hoy.plusDays(1))), Pastillas.siguiente(a(23, 0), h))

    @Test fun comprobacionNocturnaDespuesDeMedianocheTambienSeEncuentra() {
        val tarde = Horario(minNoche = 23 * 60)   // 23:00 + 2 h = 01:00 del día siguiente
        assertEquals(Evento(TipoEvento.COMPROBAR, Toma.NOCHE, hoy, a(1, 0, hoy.plusDays(1))), Pastillas.siguiente(a(0, 10, hoy.plusDays(1)), tarde))
    }

    @Test fun siguienteDesdeElPropioEventoDevuelveElPosterior() =
        assertEquals(a(9, 50), Pastillas.siguiente(a(8, 50), h).cuando)

    @Test fun horaProgramadaDeUnEvento() {
        val ev = Evento(TipoEvento.COMPROBAR, Toma.NOCHE, hoy, a(0, 0))   // el Intent no guarda la hora
        assertEquals(a(22, 50), Pastillas.horaProgramada(ev, h))
    }

    // ---------- avisar ----------
    @Test fun avisaSiNoEstaMarcada() =
        assertTrue(Pastillas.debeAvisar(Evento(TipoEvento.COMPROBAR, Toma.MANANA, hoy, a(9, 50)), TomasHechas(), null))

    @Test fun noAvisaSiEstaMarcada() =
        assertFalse(Pastillas.debeAvisar(Evento(TipoEvento.COMPROBAR, Toma.MANANA, hoy, a(9, 50)), TomasHechas(manana = hoy.toString()), null))

    @Test fun recordarNuncaAvisaALaFamilia() =
        assertFalse(Pastillas.debeAvisar(Evento(TipoEvento.RECORDAR, Toma.MANANA, hoy, a(8, 50)), TomasHechas(), null))

    @Test fun noAvisaDeTomasAnterioresALaActivacion() =
        assertFalse(Pastillas.debeAvisar(Evento(TipoEvento.COMPROBAR, Toma.NOCHE, hoy.minusDays(1), a(22, 50, hoy.minusDays(1))), TomasHechas(), desde = hoy))

    // ---------- horas ----------
    @Test fun parsearHoraAceptaFormatosHabituales() {
        assertEquals(530, Pastillas.parsearHora("08:50"))
        assertEquals(530, Pastillas.parsearHora("8:50"))
        assertEquals(530, Pastillas.parsearHora("8.50"))
        assertEquals(530, Pastillas.parsearHora(" 08h50 "))
    }

    @Test fun parsearHoraRechazaBasura() {
        assertNull(Pastillas.parsearHora("25:00"))
        assertNull(Pastillas.parsearHora("8:5"))
        assertNull(Pastillas.parsearHora("abc"))
        assertNull(Pastillas.parsearHora(""))
    }

    @Test fun formatearHora() = assertEquals("08:50", Pastillas.formatearHora(530))

    @Test fun horarioAlRevesNoEsValido() {
        assertTrue(Horario().valido)
        assertFalse(Horario(minManana = 21 * 60, minNoche = 1250).valido)
    }

    // ---------- zona horaria ----------
    @Test fun aMillisRespetaElCambioDeHoraDeVerano() {
        val madrid = ZoneId.of("Europe/Madrid")
        // 28/03/2027: en Madrid se adelanta la hora (CET → CEST, UTC+2). 08:50 locales = 06:50 UTC.
        val millis = Pastillas.aMillis(LocalDateTime.of(2027, 3, 28, 8, 50), madrid)
        assertEquals(java.time.Instant.parse("2027-03-28T06:50:00Z").toEpochMilli(), millis)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.fabri.nonna.pastillas.PastillasTest"`
Expected: FAIL de compilación con `Unresolved reference 'Pastillas'` / `'Horario'`.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.fabri.nonna.pastillas

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

enum class Toma { MANANA, NOCHE }
enum class TipoEvento { RECORDAR, COMPROBAR }

/** Minutos desde medianoche. Por defecto 08:50 y 20:50; avisar a la familia 1 h y 2 h después. */
data class Horario(
    val minManana: Int = 8 * 60 + 50,
    val minNoche: Int = 20 * 60 + 50,
    val esperaManana: Int = 60,
    val esperaNoche: Int = 120
) {
    val valido: Boolean get() = minManana in 0 until 1440 && minNoche in 0 until 1440 && minManana < minNoche
}

/** Fecha ISO (yyyy-MM-dd) de la última toma marcada de cada tipo. */
@Serializable
data class TomasHechas(val manana: String? = null, val noche: String? = null)

/** La toma de la noche del día D sigue pendiente hasta la mañana de D+1, por eso lleva su fecha. */
data class Pendiente(val toma: Toma, val fecha: LocalDate)

data class Evento(val tipo: TipoEvento, val toma: Toma, val fecha: LocalDate, val cuando: LocalDateTime)

object Pastillas {

    fun hecha(h: TomasHechas, toma: Toma, fecha: LocalDate): Boolean =
        (if (toma == Toma.MANANA) h.manana else h.noche) == fecha.toString()

    fun pendiente(ahora: LocalDateTime, horario: Horario, hechas: TomasHechas, desde: LocalDate?): Pendiente? {
        val hoy = ahora.toLocalDate()
        val min = ahora.hour * 60 + ahora.minute
        val p = when {
            min >= horario.minNoche -> Pendiente(Toma.NOCHE, hoy)
            min >= horario.minManana -> Pendiente(Toma.MANANA, hoy)
            else -> Pendiente(Toma.NOCHE, hoy.minusDays(1))
        }
        if (desde != null && p.fecha.isBefore(desde)) return null
        return p.takeUnless { hecha(hechas, it.toma, it.fecha) }
    }

    fun marcar(h: TomasHechas, p: Pendiente): TomasHechas =
        if (p.toma == Toma.MANANA) h.copy(manana = p.fecha.toString()) else h.copy(noche = p.fecha.toString())

    fun eventosDe(fecha: LocalDate, horario: Horario): List<Evento> {
        val m = fecha.atStartOfDay().plusMinutes(horario.minManana.toLong())
        val n = fecha.atStartOfDay().plusMinutes(horario.minNoche.toLong())
        return listOf(
            Evento(TipoEvento.RECORDAR, Toma.MANANA, fecha, m),
            Evento(TipoEvento.COMPROBAR, Toma.MANANA, fecha, m.plusMinutes(horario.esperaManana.toLong())),
            Evento(TipoEvento.RECORDAR, Toma.NOCHE, fecha, n),
            Evento(TipoEvento.COMPROBAR, Toma.NOCHE, fecha, n.plusMinutes(horario.esperaNoche.toLong()))
        )
    }

    /** Primer evento estrictamente posterior a [ahora]. Incluye ayer por si la comprobación nocturna cruza la medianoche. */
    fun siguiente(ahora: LocalDateTime, horario: Horario): Evento {
        val d = ahora.toLocalDate()
        return listOf(d.minusDays(1), d, d.plusDays(1))
            .flatMap { eventosDe(it, horario) }
            .filter { it.cuando.isAfter(ahora) }
            .minBy { it.cuando }
    }

    /** Hora a la que estaba programado [ev] según el horario (el Intent de la alarma no la guarda). */
    fun horaProgramada(ev: Evento, horario: Horario): LocalDateTime =
        eventosDe(ev.fecha, horario).first { it.tipo == ev.tipo && it.toma == ev.toma }.cuando

    fun debeAvisar(ev: Evento, hechas: TomasHechas, desde: LocalDate?): Boolean =
        ev.tipo == TipoEvento.COMPROBAR &&
            (desde == null || !ev.fecha.isBefore(desde)) &&
            !hecha(hechas, ev.toma, ev.fecha)

    private val formatoHora = Regex("""^\s*(\d{1,2})\s*[:.hH]\s*(\d{2})\s*$""")

    fun parsearHora(s: String): Int? {
        val m = formatoHora.find(s) ?: return null
        val hh = m.groupValues[1].toInt()
        val mm = m.groupValues[2].toInt()
        return if (hh in 0..23 && mm in 0..59) hh * 60 + mm else null
    }

    fun formatearHora(min: Int): String = "%02d:%02d".format(min / 60, min % 60)

    fun nombre(toma: Toma): String = if (toma == Toma.MANANA) "de la mañana" else "de la noche"

    fun aMillis(ldt: LocalDateTime, zona: ZoneId): Long = ldt.atZone(zona).toInstant().toEpochMilli()
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "com.fabri.nonna.pastillas.PastillasTest"`
Expected: PASS (todos los tests de `PastillasTest`).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/fabri/nonna/pastillas/Pastillas.kt app/src/test/java/com/fabri/nonna/pastillas/PastillasTest.kt
git commit -m "feat(pastillas): lógica pura de tomas pendientes y calendario de avisos"
```

---

### Task 2: Aviso a la familia por ntfy

**Files:**
- Create: `app/src/main/java/com/fabri/nonna/pastillas/Ntfy.kt`
- Test: `app/src/test/java/com/fabri/nonna/pastillas/NtfyTest.kt`

**Interfaces:**
- Consumes: `Toma`, `Pastillas.nombre(toma)` (Task 1).
- Produces:
  - `data class AvisoNtfy(canal: String, titulo: String, mensaje: String)` con `fun json(): String`
  - `object Ntfy` con `SERVIDOR = "https://ntfy.sh"`, `nuevoCanal(aleatorio: java.util.Random = SecureRandom()): String`, `canalValido(c: String): Boolean`, `noTomada(canal, toma): AvisoNtfy`, `prueba(canal): AvisoNtfy`, `suspend fun enviar(a: AvisoNtfy): Boolean`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.fabri.nonna.pastillas

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class NtfyTest {
    @Test fun canalNuevoTieneElFormatoEsperado() {
        val c = Ntfy.nuevoCanal(Random(42))
        assertTrue(c, Regex("^nonna-[0-9a-f]{16}$").matches(c))
        assertTrue(Ntfy.canalValido(c))
    }

    @Test fun dosCanalesNuevosSonDistintos() =
        assertTrue(Ntfy.nuevoCanal() != Ntfy.nuevoCanal())

    @Test fun canalesInvalidos() {
        assertFalse(Ntfy.canalValido(""))
        assertFalse(Ntfy.canalValido("con espacios"))
        assertFalse(Ntfy.canalValido("a/b"))
        assertFalse(Ntfy.canalValido("x".repeat(65)))
    }

    @Test fun avisoNoTomadaLlevaCanalYTextoEnEspanol() {
        val o = Json.parseToJsonElement(Ntfy.noTomada("nonna-abc", Toma.MANANA).json()).jsonObject
        assertEquals("nonna-abc", o["topic"]!!.jsonPrimitive.content)
        assertEquals("💊 La Nonna no ha marcado las pastillas de la mañana", o["message"]!!.jsonPrimitive.content)
        assertEquals("Pastillas de la Nonna", o["title"]!!.jsonPrimitive.content)
        assertEquals("pill", o["tags"]!!.jsonArray[0].jsonPrimitive.content)
    }

    @Test fun avisoDeLaNoche() =
        assertTrue(Ntfy.noTomada("nonna-abc", Toma.NOCHE).mensaje.endsWith("de la noche"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.fabri.nonna.pastillas.NtfyTest"`
Expected: FAIL de compilación con `Unresolved reference 'Ntfy'`.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.fabri.nonna.pastillas

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom

data class AvisoNtfy(val canal: String, val titulo: String, val mensaje: String) {
    /** Publicación JSON de ntfy: el cuerpo va en UTF-8, así los acentos y emojis llegan bien. */
    fun json(): String = JsonObject(
        mapOf(
            "topic" to JsonPrimitive(canal),
            "title" to JsonPrimitive(titulo),
            "message" to JsonPrimitive(mensaje),
            "tags" to JsonArray(listOf(JsonPrimitive("pill")))
        )
    ).toString()
}

object Ntfy {
    const val SERVIDOR = "https://ntfy.sh"
    private val formatoCanal = Regex("^[A-Za-z0-9_-]{1,64}$")

    fun nuevoCanal(aleatorio: java.util.Random = SecureRandom()): String =
        "nonna-" + (1..16).map { "0123456789abcdef"[aleatorio.nextInt(16)] }.joinToString("")

    fun canalValido(c: String): Boolean = formatoCanal.matches(c)

    fun noTomada(canal: String, toma: Toma) =
        AvisoNtfy(canal, "Pastillas de la Nonna", "💊 La Nonna no ha marcado las pastillas ${Pastillas.nombre(toma)}")

    fun prueba(canal: String) =
        AvisoNtfy(canal, "Inicio Nonna", "✅ Aviso de prueba: así te llegará si la Nonna no marca sus pastillas")

    suspend fun enviar(a: AvisoNtfy): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val con = URL(SERVIDOR).openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.connectTimeout = 8_000; con.readTimeout = 8_000
            con.doOutput = true
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            try {
                con.outputStream.use { it.write(a.json().toByteArray(Charsets.UTF_8)) }
                con.responseCode in 200..299
            } finally { con.disconnect() }
        }.getOrDefault(false)
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "com.fabri.nonna.pastillas.NtfyTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/fabri/nonna/pastillas/Ntfy.kt app/src/test/java/com/fabri/nonna/pastillas/NtfyTest.kt
git commit -m "feat(pastillas): aviso a la familia por ntfy"
```

---

### Task 3: Ajustes y registro de tomas en el almacenamiento

**Files:**
- Modify: `app/src/main/java/com/fabri/nonna/datos/Modelos.kt` (data class `Ajustes`)
- Modify: `app/src/main/java/com/fabri/nonna/datos/Codec.kt`
- Modify: `app/src/main/java/com/fabri/nonna/datos/Repositorio.kt`
- Create: `app/src/main/java/com/fabri/nonna/pastillas/AjustesPastillas.kt`
- Test: `app/src/test/java/com/fabri/nonna/datos/CodecTest.kt`

**Interfaces:**
- Consumes: `TomasHechas`, `Pendiente`, `Horario`, `Pastillas.marcar` (Task 1).
- Produces:
  - Campos nuevos en `Ajustes`: `pastillasActivas: Boolean = false`, `pastillaMinManana: Int = 530`, `pastillaMinNoche: Int = 1250`, `pastillaVozManana: Boolean = true`, `pastillasDesde: String = ""`, `ntfyCanal: String = ""`
  - `Codec.tomas(s: String?): TomasHechas`, `Codec.texto(t: TomasHechas): String`
  - `Repositorio.tomas: Flow<TomasHechas>`, `suspend fun Repositorio.marcarToma(p: Pendiente)`
  - `fun Ajustes.horario(): Horario`, `fun Ajustes.desde(): LocalDate?`

- [ ] **Step 1: Write the failing test** — añadir al final de la clase `CodecTest`:

```kotlin
    @Test fun ajustesAntiguosSinCamposDePastillasUsanLosValoresPorDefecto() {
        val a = Codec.ajustes("""{"nombreAyuda":"Fabri","configurado":true}""")
        assertEquals(false, a.pastillasActivas)
        assertEquals(530, a.pastillaMinManana)
        assertEquals(1250, a.pastillaMinNoche)
        assertEquals("", a.ntfyCanal)
    }
    @Test fun tomasIdaYVuelta() {
        val t = com.fabri.nonna.pastillas.TomasHechas(manana = "2026-11-10", noche = null)
        assertEquals(t, Codec.tomas(Codec.texto(t)))
    }
    @Test fun tomasCorruptasOVaciasSonVacias() {
        assertEquals(com.fabri.nonna.pastillas.TomasHechas(), Codec.tomas("{basura"))
        assertEquals(com.fabri.nonna.pastillas.TomasHechas(), Codec.tomas(null))
    }
    @Test fun desdeVacioEsNullYFechaSeLee() {
        assertNull(Ajustes().let { with(com.fabri.nonna.pastillas.AjustesPastillas) { it.desde() } })
        assertEquals(java.time.LocalDate.of(2026, 11, 10), Ajustes(pastillasDesde = "2026-11-10").let { with(com.fabri.nonna.pastillas.AjustesPastillas) { it.desde() } })
    }
```

> Nota: `horario()` y `desde()` se definen como extensiones dentro de `object AjustesPastillas` para poder importarlas con `import com.fabri.nonna.pastillas.AjustesPastillas.desde` en el código de producción; el test usa `with(...)` para no depender de imports.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.fabri.nonna.datos.CodecTest"`
Expected: FAIL de compilación (`pastillasActivas`, `Codec.tomas`, `AjustesPastillas` no existen).

- [ ] **Step 3: Write minimal implementation**

En `Modelos.kt`, sustituir la data class `Ajustes` por:

```kotlin
@Serializable
data class Ajustes(
    val ciudad: String = "",
    val lat: Double? = null,
    val lon: Double? = null,
    val tarjetaNumero: String = "",
    val tarjetaTipo: TipoTarjeta = TipoTarjeta.QR,
    val vozActivada: Boolean = true,
    val nombreAyuda: String = "Fabri",
    val pinHash: String = "",
    val pinSal: String = "",
    val configurado: Boolean = false,
    // ---------- Pastillas ----------
    val pastillasActivas: Boolean = false,
    val pastillaMinManana: Int = 8 * 60 + 50,
    val pastillaMinNoche: Int = 20 * 60 + 50,
    val pastillaVozManana: Boolean = true,
    /** Fecha ISO en que se activaron: las tomas anteriores nunca cuentan como pendientes. */
    val pastillasDesde: String = "",
    val ntfyCanal: String = ""
)
```

En `Codec.kt`, añadir el import `import com.fabri.nonna.pastillas.TomasHechas` y estas dos funciones dentro de `object Codec`:

```kotlin
    fun tomas(s: String?): TomasHechas =
        s?.let { runCatching { json.decodeFromString<TomasHechas>(it) }.getOrNull() } ?: TomasHechas()
    fun texto(t: TomasHechas): String = json.encodeToString(t)
```

En `Repositorio.kt`, añadir los imports `import com.fabri.nonna.pastillas.Pastillas`, `import com.fabri.nonna.pastillas.Pendiente`, `import com.fabri.nonna.pastillas.TomasHechas`, y dentro de la clase:

```kotlin
    private val kTomas = stringPreferencesKey("tomas")

    val tomas: Flow<TomasHechas> = prefs.map { Codec.tomas(it[kTomas]) }

    /** Lectura y escritura en la misma transacción: no se pierde una marca aunque lleguen dos a la vez. */
    suspend fun marcarToma(p: Pendiente) {
        ctx.almacen.edit { it[kTomas] = Codec.texto(Pastillas.marcar(Codec.tomas(it[kTomas]), p)) }
    }
```

(Declarar `kTomas` junto a las otras claves, antes de `prefs`.)

Crear `AjustesPastillas.kt`:

```kotlin
package com.fabri.nonna.pastillas

import com.fabri.nonna.datos.Ajustes
import java.time.LocalDate

object AjustesPastillas {
    fun Ajustes.horario(): Horario = Horario(minManana = pastillaMinManana, minNoche = pastillaMinNoche)
    fun Ajustes.desde(): LocalDate? = pastillasDesde.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest`
Expected: PASS (todos los tests del proyecto, incluidos los antiguos de `CodecTest`).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/fabri/nonna/datos app/src/main/java/com/fabri/nonna/pastillas/AjustesPastillas.kt app/src/test/java/com/fabri/nonna/datos/CodecTest.kt
git commit -m "feat(pastillas): ajustes y registro de tomas en DataStore"
```

---

### Task 4: Alarmas, receptor, notificaciones y voz

**Files:**
- Create: `app/src/main/java/com/fabri/nonna/pastillas/AvisosPastillas.kt`
- Create: `app/src/main/java/com/fabri/nonna/pastillas/VozBreve.kt`
- Create: `app/src/main/java/com/fabri/nonna/pastillas/ProgramadorPastillas.kt`
- Create: `app/src/main/java/com/fabri/nonna/pastillas/ReceptorPastillas.kt`
- Create: `app/src/main/res/drawable/ic_pastilla.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/fabri/nonna/MainActivity.kt`

**Interfaces:**
- Consumes: Task 1 (`Pastillas`, `Evento`, `TipoEvento`, `Toma`), Task 2 (`Ntfy`), Task 3 (`Repositorio.ajustes`, `Repositorio.tomas`, `AjustesPastillas.horario/desde`).
- Produces:
  - `object ProgramadorPastillas { fun programar(ctx: Context, ajustes: Ajustes, desde: LocalDateTime = LocalDateTime.now()); fun reintentar(ctx: Context, ev: Evento, intento: Int); fun dispararAhora(ctx: Context, tipo: TipoEvento, toma: Toma); fun eventoDe(i: Intent): Evento? }`
  - `object AvisosPastillas { fun mostrar(ctx: Context, toma: Toma); fun quitar(ctx: Context, toma: Toma); fun permitidas(ctx: Context): Boolean }`
  - `object VozBreve { suspend fun decir(ctx: Context, texto: String) }`
  - `class ReceptorPastillas : BroadcastReceiver` con `companion object { const val ACCION_EVENTO = "com.fabri.nonna.PASTILLA_EVENTO" }`

Esta tarea no tiene tests unitarios (todo es API de Android); la lógica que decide qué hacer ya está testeada en Task 1. Se verifica compilando y en el Redmi en Task 7.

- [ ] **Step 1: Permisos y receptor en el manifiesto**

Añadir junto a los otros `uses-permission`:

```xml
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <!-- Alarmas exactas para las pastillas: USE_EXACT_ALARM se concede solo en Android 13+; en 12 hace falta SCHEDULE_EXACT_ALARM -->
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" android:maxSdkVersion="32" />
```

Y dentro de `<application>`, después del `<service>`:

```xml
        <receiver
            android:name=".pastillas.ReceptorPastillas"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
                <action android:name="android.intent.action.TIME_SET" />
                <action android:name="android.intent.action.TIMEZONE_CHANGED" />
            </intent-filter>
        </receiver>
```

- [ ] **Step 2: Icono de la notificación** — crear `app/src/main/res/drawable/ic_pastilla.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF"
        android:pathData="M4.22,11.29l7.07,-7.07c2.34,-2.34 6.14,-2.34 8.48,0s2.34,6.14 0,8.48l-7.07,7.07c-2.34,2.34 -6.14,2.34 -8.48,0s-2.34,-6.14 0,-8.48zM5.64,12.71c-1.56,1.56 -1.56,4.09 0,5.66s4.09,1.56 5.66,0l3.53,-3.54 -5.65,-5.66 -3.54,3.54z"/>
</vector>
```

- [ ] **Step 3: Notificaciones** — crear `AvisosPastillas.kt`:

```kotlin
package com.fabri.nonna.pastillas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fabri.nonna.MainActivity
import com.fabri.nonna.R

object AvisosPastillas {
    private const val CANAL_MANANA = "pastillas_manana"   // sonido suave + vibración
    private const val CANAL_NOCHE = "pastillas_noche"     // solo vibración: puede estar en la calle

    fun permitidas(ctx: Context): Boolean = NotificationManagerCompat.from(ctx).areNotificationsEnabled()

    private fun crearCanales(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CANAL_MANANA, "Pastillas de la mañana", NotificationManager.IMPORTANCE_HIGH).apply { enableVibration(true) }
        )
        nm.createNotificationChannel(
            NotificationChannel(CANAL_NOCHE, "Pastillas de la noche", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null); enableVibration(true)
            }
        )
    }

    private fun id(toma: Toma) = 7000 + toma.ordinal

    fun mostrar(ctx: Context, toma: Toma) {
        if (!permitidas(ctx)) return
        crearCanales(ctx)
        val abrir = PendingIntent.getActivity(
            ctx, id(toma), Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(ctx, if (toma == Toma.MANANA) CANAL_MANANA else CANAL_NOCHE)
            .setSmallIcon(R.drawable.ic_pastilla)
            .setContentTitle("💊 Pastillas ${Pastillas.nombre(toma)}")
            .setContentText("Toca aquí cuando te las tomes")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(ctx).notify(id(toma), n) }
    }

    fun quitar(ctx: Context, toma: Toma) = NotificationManagerCompat.from(ctx).cancel(id(toma))
}
```

- [ ] **Step 4: Voz desde el receptor** — crear `VozBreve.kt`:

```kotlin
package com.fabri.nonna.pastillas

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/** TTS de un solo uso (el receptor no tiene la Voz de MainActivity). Como mucho 8 s; se apaga al terminar. */
object VozBreve {
    suspend fun decir(ctx: Context, texto: String) {
        withTimeoutOrNull(8_000) {
            suspendCancellableCoroutine { cont ->
                var tts: TextToSpeech? = null
                fun terminar() { tts?.shutdown(); if (cont.isActive) cont.resume(Unit) }
                tts = TextToSpeech(ctx.applicationContext) { estado ->
                    val t = tts ?: return@TextToSpeech
                    if (estado != TextToSpeech.SUCCESS) { terminar(); return@TextToSpeech }
                    t.language = Locale.forLanguageTag("es-ES")
                    t.setAudioAttributes(
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
                    )
                    t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(id: String?) {}
                        override fun onDone(id: String?) = terminar()
                        @Deprecated("API antigua") override fun onError(id: String?) = terminar()
                    })
                    t.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "pastilla")
                }
                cont.invokeOnCancellation { tts?.shutdown() }
            }
        }
    }
}
```

- [ ] **Step 5: Programador** — crear `ProgramadorPastillas.kt`:

```kotlin
package com.fabri.nonna.pastillas

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fabri.nonna.MainActivity
import com.fabri.nonna.datos.Ajustes
import com.fabri.nonna.pastillas.AjustesPastillas.horario
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object ProgramadorPastillas {
    private const val RC_SIGUIENTE = 1
    private const val RC_REINTENTO = 2
    private const val EXTRA_TIPO = "tipo"
    private const val EXTRA_TOMA = "toma"
    private const val EXTRA_FECHA = "fecha"
    const val EXTRA_INTENTO = "intento"
    private const val MAX_REINTENTOS = 3

    private fun intent(ctx: Context, ev: Evento?, intento: Int) =
        Intent(ctx, ReceptorPastillas::class.java).setAction(ReceptorPastillas.ACCION_EVENTO).apply {
            if (ev != null) {
                putExtra(EXTRA_TIPO, ev.tipo.name); putExtra(EXTRA_TOMA, ev.toma.name)
                putExtra(EXTRA_FECHA, ev.fecha.toString()); putExtra(EXTRA_INTENTO, intento)
            }
        }

    private fun pi(ctx: Context, rc: Int, ev: Evento?, intento: Int = 0) = PendingIntent.getBroadcast(
        ctx, rc, intent(ctx, ev, intento), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun eventoDe(i: Intent): Evento? = runCatching {
        val tipo = TipoEvento.valueOf(i.getStringExtra(EXTRA_TIPO)!!)
        val toma = Toma.valueOf(i.getStringExtra(EXTRA_TOMA)!!)
        val fecha = LocalDate.parse(i.getStringExtra(EXTRA_FECHA)!!)
        Evento(tipo, toma, fecha, LocalDateTime.now())
    }.getOrNull()

    private fun exactasPermitidas(am: AlarmManager) = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()

    /** Programa (o cancela) la única alarma: el siguiente evento posterior a [desde]. */
    fun programar(ctx: Context, ajustes: Ajustes, desde: LocalDateTime = LocalDateTime.now()) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        val h = ajustes.horario()
        if (!ajustes.pastillasActivas || !h.valido) { am.cancel(pi(ctx, RC_SIGUIENTE, null)); return }
        val ev = Pastillas.siguiente(desde, h)
        val millis = Pastillas.aMillis(ev.cuando, ZoneId.systemDefault())
        val alarma = pi(ctx, RC_SIGUIENTE, ev)
        if (exactasPermitidas(am)) {
            val abrir = PendingIntent.getActivity(
                ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.setAlarmClock(AlarmManager.AlarmClockInfo(millis, abrir), alarma)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, alarma)
        }
    }

    /** Sin internet al avisar a la familia: vuelve a intentarlo en 10 minutos, como mucho 3 veces. */
    fun reintentar(ctx: Context, ev: Evento, intento: Int) {
        if (intento >= MAX_REINTENTOS) return
        val am = ctx.getSystemService(AlarmManager::class.java)
        am.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10 * 60_000, pi(ctx, RC_REINTENTO, ev, intento + 1)
        )
    }

    /** Para "Simular…" en Comprobar botones: entrega el evento ahora mismo a nuestro propio receptor. */
    fun dispararAhora(ctx: Context, tipo: TipoEvento, toma: Toma) {
        val ev = Evento(tipo, toma, LocalDate.now(), LocalDateTime.now())
        ctx.sendBroadcast(intent(ctx, ev, 0).setPackage(ctx.packageName))
    }
}
```

- [ ] **Step 6: Receptor** — crear `ReceptorPastillas.kt`:

```kotlin
package com.fabri.nonna.pastillas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fabri.nonna.BuildConfig
import com.fabri.nonna.datos.Ajustes
import com.fabri.nonna.datos.Repositorio
import com.fabri.nonna.pastillas.AjustesPastillas.desde
import com.fabri.nonna.pastillas.AjustesPastillas.horario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ReceptorPastillas : BroadcastReceiver() {

    companion object {
        const val ACCION_EVENTO = "com.fabri.nonna.PASTILLA_EVENTO"
        private const val TAG = "NONNA_PASTILLAS"
        private const val VOZ_MANANA = "Buenos días, Nonna. Toca la pastilla de la mañana."
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        val resultado = goAsync()
        val app = ctx.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val repo = Repositorio(app)
                val ajustes = repo.ajustes.first()
                var reprogramarDesde = LocalDateTime.now()
                if (intent.action == ACCION_EVENTO) {
                    val ev = ProgramadorPastillas.eventoDe(intent)
                    if (ev != null && ajustes.pastillasActivas) {
                        atender(app, repo, ajustes, ev, intent.getIntExtra(ProgramadorPastillas.EXTRA_INTENTO, 0))
                    }
                    // Si la alarma llega unos ms antes, no volver a programar el mismo evento.
                    if (ev != null) reprogramarDesde = maxOf(reprogramarDesde, Pastillas.horaProgramada(ev, ajustes.horario()))
                }
                ProgramadorPastillas.programar(app, ajustes, reprogramarDesde)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.w(TAG, "fallo atendiendo ${intent.action}", e)
            } finally {
                resultado.finish()
            }
        }
    }

    private suspend fun atender(ctx: Context, repo: Repositorio, ajustes: Ajustes, ev: Evento, intento: Int) {
        if (BuildConfig.DEBUG) Log.i(TAG, "evento $ev intento=$intento")
        when (ev.tipo) {
            TipoEvento.RECORDAR -> {
                AvisosPastillas.mostrar(ctx, ev.toma)
                if (ev.toma == Toma.MANANA && ajustes.pastillaVozManana) VozBreve.decir(ctx, VOZ_MANANA)
            }
            TipoEvento.COMPROBAR -> {
                if (!Pastillas.debeAvisar(ev, repo.tomas.first(), ajustes.desde())) return
                if (!Ntfy.canalValido(ajustes.ntfyCanal)) return
                val ok = Ntfy.enviar(Ntfy.noTomada(ajustes.ntfyCanal, ev.toma))
                if (BuildConfig.DEBUG) Log.i(TAG, "ntfy -> $ok")
                if (!ok) ProgramadorPastillas.reintentar(ctx, ev, intento)
            }
        }
    }
}
```

El evento reconstruido desde el Intent lleva `cuando = now()`; por eso se reprograma desde `Pastillas.horaProgramada(...)` (Task 1) y no desde `ev.cuando`.

- [ ] **Step 7: Reprogramar al volver a la app** — en `MainActivity.kt`, añadir imports:

```kotlin
import androidx.lifecycle.lifecycleScope
import com.fabri.nonna.pastillas.ProgramadorPastillas
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
```

y dentro de `onResume()`, al final:

```kotlin
        lifecycleScope.launch { ProgramadorPastillas.programar(this@MainActivity, repo.ajustes.first()) }
```

- [ ] **Step 8: Compilar y pasar todos los tests**

Run: `./gradlew testDebugUnitTest assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit**

```bash
git add app/src/main
git commit -m "feat(pastillas): alarma exacta, receptor, notificaciones, voz y aviso ntfy"
```

---

### Task 5: Franja en Inicio y pantalla para marcar

**Files:**
- Create: `app/src/main/java/com/fabri/nonna/ui/home/FranjaPastilla.kt`
- Create: `app/src/main/java/com/fabri/nonna/ui/pastillas/PastillaScreen.kt`
- Modify: `app/src/main/java/com/fabri/nonna/ui/home/HomeScreen.kt`
- Modify: `app/src/main/java/com/fabri/nonna/navegacion/Pantalla.kt`
- Modify: `app/src/main/java/com/fabri/nonna/navegacion/AppAbuela.kt`

**Interfaces:**
- Consumes: `Pendiente`, `Toma`, `Pastillas.pendiente/nombre` (Task 1); `Repositorio.tomas`, `Repositorio.marcarToma`, `AjustesPastillas.horario/desde` (Task 3); `AvisosPastillas.quitar` (Task 4); `Controlador.permitir`, `Controlador.nuevaPantallaDeConfirmacion` (existentes).
- Produces: `Pantalla.Pastilla(pendiente: Pendiente)`; `HomeScreen(..., pastilla: Pendiente?, onPastilla: () -> Unit)`.

- [ ] **Step 1: Franja** — crear `FranjaPastilla.kt`:

```kotlin
package com.fabri.nonna.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fabri.nonna.pastillas.Pastillas
import com.fabri.nonna.pastillas.Pendiente
import com.fabri.nonna.ui.components.BotonGrande

@Composable
fun FranjaPastilla(p: Pendiente, onClick: () -> Unit) {
    BotonGrande(
        emoji = "💊",
        titulo = "PASTILLAS ${Pastillas.nombre(p.toma).uppercase()}",
        subtitulo = "Toca aquí cuando te las tomes",
        color = Color(0xFFCE93D8),
        modifier = Modifier.fillMaxWidth().height(150.dp),
        onClick = onClick
    )
}
```

- [ ] **Step 2: Pantalla de confirmación** — crear `PastillaScreen.kt`:

```kotlin
package com.fabri.nonna.ui.pastillas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.fabri.nonna.pastillas.Pastillas
import com.fabri.nonna.pastillas.Pendiente
import com.fabri.nonna.ui.components.BotonGrande
import com.fabri.nonna.ui.theme.Colores
import com.fabri.nonna.ui.theme.Medidas

@Composable
fun PastillaScreen(p: Pendiente, onSi: () -> Unit, onTodaviaNo: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Medidas.separacion)
    ) {
        Text("💊", fontSize = 96.sp)
        Text(
            "¿Te has tomado las pastillas ${Pastillas.nombre(p.toma)}?",
            fontSize = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
        )
        BotonGrande("✅", "SÍ, YA ME LAS TOMÉ", null, Colores.si, Modifier.fillMaxWidth().weight(1f), contentColor = Color.White, onClick = onSi)
        BotonGrande("⏳", "TODAVÍA NO", null, Colores.volver, Modifier.fillMaxWidth().weight(1f), onClick = onTodaviaNo)
    }
}
```

- [ ] **Step 3: Nueva pantalla en la navegación** — en `Pantalla.kt` añadir el import `import com.fabri.nonna.pastillas.Pendiente` y dentro de la interfaz:

```kotlin
    data class Pastilla(val pendiente: Pendiente) : Pantalla
```

- [ ] **Step 4: Mostrar la franja en Inicio** — en `HomeScreen.kt`, añadir el import `import com.fabri.nonna.pastillas.Pendiente`, añadir dos parámetros al final de la firma:

```kotlin
    onDia: () -> Unit,
    pastilla: Pendiente? = null,
    onPastilla: () -> Unit = {}
```

y justo después de `Cabecera(temperatura, onToqueReloj)`:

```kotlin
        if (pastilla != null) FranjaPastilla(pastilla, onPastilla)
```

- [ ] **Step 5: Cablear en AppAbuela** — en `AppAbuela.kt`:

Imports nuevos:

```kotlin
import com.fabri.nonna.pastillas.AjustesPastillas.desde
import com.fabri.nonna.pastillas.AjustesPastillas.horario
import com.fabri.nonna.pastillas.AvisosPastillas
import com.fabri.nonna.pastillas.Pastillas
import com.fabri.nonna.pastillas.ProgramadorPastillas
import com.fabri.nonna.pastillas.TomasHechas
import com.fabri.nonna.ui.pastillas.PastillaScreen
import java.time.LocalDateTime
```

Estado (junto a los otros `collectAsState`):

```kotlin
    val tomas by repo.tomas.collectAsState(initial = TomasHechas())
    // Reloj propio para que la franja aparezca a su hora aunque nadie toque la pantalla.
    val ahora by produceState(LocalDateTime.now()) { while (true) { delay(30_000); value = LocalDateTime.now() } }
    val pastillaPendiente = if (ajustes.pastillasActivas && ajustes.horario().valido)
        Pastillas.pendiente(ahora, ajustes.horario(), tomas, ajustes.desde()) else null
```

En `BackHandler`, añadir el caso (antes de `else ->`):

```kotlin
            is Pantalla.Pastilla -> Pantalla.Inicio
```

En la llamada a `HomeScreen(...)`, añadir los argumentos:

```kotlin
                pastilla = pastillaPendiente,
                onPastilla = {
                    pastillaPendiente?.let { p ->
                        if (controlador.permitir("¿Te has tomado las pastillas ${Pastillas.nombre(p.toma)}?")) pantalla = Pantalla.Pastilla(p)
                    }
                }
```

Nuevo caso en el `when (val p = pantalla)` (junto a `Pantalla.TarjetaDia`):

```kotlin
        is Pantalla.Pastilla -> {
            // Igual que en las opciones de un familiar: un doble toque en la franja no marca SÍ sin querer.
            LaunchedEffect(p) { controlador.nuevaPantallaDeConfirmacion() }
            PastillaScreen(
                p.pendiente,
                onSi = {
                    if (controlador.permitir("¡Muy bien, Nonna!")) {
                        scope.launch { repo.marcarToma(p.pendiente) }
                        AvisosPastillas.quitar(ctx, p.pendiente.toma)
                        pantalla = Pantalla.Inicio
                    }
                },
                onTodaviaNo = { pantalla = Pantalla.Inicio }
            )
        }
```

En `Pantalla.AjustesAdmin -> AjustesAdminScreen(... onGuardar = ...)`, reprogramar tras guardar:

```kotlin
            onGuardar = { nuevos ->
                scope.launch {
                    repo.guardarAjustes(nuevos)
                    ProgramadorPastillas.programar(ctx, nuevos)
                    pantalla = Pantalla.Admin
                }
            },
```

- [ ] **Step 6: Compilar y pasar todos los tests**

Run: `./gradlew testDebugUnitTest assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/fabri/nonna/ui app/src/main/java/com/fabri/nonna/navegacion
git commit -m "feat(pastillas): franja en Inicio y pantalla SÍ / TODAVÍA NO"
```

---

### Task 6: Configuración en admin y comprobaciones

**Files:**
- Create: `app/src/main/java/com/fabri/nonna/ui/admin/SeccionPastillas.kt`
- Modify: `app/src/main/java/com/fabri/nonna/ui/admin/AjustesAdminScreen.kt`
- Modify: `app/src/main/java/com/fabri/nonna/admin/Diagnostico.kt`
- Modify: `app/src/main/java/com/fabri/nonna/ui/admin/DiagnosticoScreen.kt`

**Interfaces:**
- Consumes: `Pastillas.parsearHora/formatearHora`, `Horario` (Task 1); `Ntfy` (Task 2); campos de `Ajustes` (Task 3); `AvisosPastillas.permitidas`, `ProgramadorPastillas.dispararAhora` (Task 4); `TituloSeccion`, `margenCampo` (existentes en `ui/admin/Armazon.kt`).
- Produces: `@Composable fun SeccionPastillas(a: Ajustes, onCambio: (Ajustes) -> Unit, textoManana: String, onTextoManana: (String) -> Unit, textoNoche: String, onTextoNoche: (String) -> Unit)` y `fun horasValidas(textoManana: String, textoNoche: String): Boolean`.

- [ ] **Step 1: Sección de Ajustes** — crear `SeccionPastillas.kt`:

```kotlin
package com.fabri.nonna.ui.admin

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.fabri.nonna.datos.Ajustes
import com.fabri.nonna.pastillas.Horario
import com.fabri.nonna.pastillas.Ntfy
import com.fabri.nonna.pastillas.Pastillas
import kotlinx.coroutines.launch
import java.time.LocalDate

fun horasValidas(textoManana: String, textoNoche: String): Boolean {
    val m = Pastillas.parsearHora(textoManana) ?: return false
    val n = Pastillas.parsearHora(textoNoche) ?: return false
    return Horario(minManana = m, minNoche = n).valido
}

@Composable
fun SeccionPastillas(
    a: Ajustes,
    onCambio: (Ajustes) -> Unit,
    textoManana: String,
    onTextoManana: (String) -> Unit,
    textoNoche: String,
    onTextoNoche: (String) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var resultadoPrueba by remember { mutableStateOf<String?>(null) }
    val pedirNotificaciones = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    TituloSeccion("Pastillas")
    ListItem(
        headlineContent = { Text("Recordar las pastillas") },
        supportingContent = { Text("Mañana y noche, con aviso a la familia si no las marca") },
        trailingContent = { Switch(a.pastillasActivas, onCheckedChange = null) },
        modifier = Modifier.clickable {
            val activar = !a.pastillasActivas
            onCambio(
                a.copy(
                    pastillasActivas = activar,
                    pastillasDesde = if (activar) LocalDate.now().toString() else a.pastillasDesde,
                    ntfyCanal = a.ntfyCanal.ifBlank { Ntfy.nuevoCanal() }
                )
            )
            if (activar && Build.VERSION.SDK_INT >= 33) pedirNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    )
    if (!a.pastillasActivas) return

    val horasOk = horasValidas(textoManana, textoNoche)
    OutlinedTextField(
        textoManana, onTextoManana, label = { Text("Hora de la mañana") }, singleLine = true,
        isError = Pastillas.parsearHora(textoManana) == null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = margenCampo.fillMaxWidth()
    )
    OutlinedTextField(
        textoNoche, onTextoNoche, label = { Text("Hora de la noche") }, singleLine = true,
        isError = Pastillas.parsearHora(textoNoche) == null || !horasOk,
        supportingText = { Text(if (horasOk) "Formato 08:50" else "Revisa las horas: la de la mañana debe ir antes") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = margenCampo.fillMaxWidth()
    )
    ListItem(
        headlineContent = { Text("Voz por la mañana") },
        supportingContent = { Text("Por la noche nunca habla: puede estar en la calle") },
        trailingContent = { Switch(a.pastillaVozManana, onCheckedChange = null) },
        modifier = Modifier.clickable { onCambio(a.copy(pastillaVozManana = !a.pastillaVozManana)) }
    )
    ListItem(
        headlineContent = { Text(a.ntfyCanal) },
        supportingContent = { Text("Canal de aviso. Instala ntfy en tu móvil → + → escribe este canal") }
    )
    Row(margenCampo) {
        TextButton(onClick = {
            ctx.getSystemService(ClipboardManager::class.java)
                .setPrimaryClip(ClipData.newPlainText("Canal ntfy", a.ntfyCanal))
        }) { Text("Copiar") }
        TextButton(onClick = { onCambio(a.copy(ntfyCanal = Ntfy.nuevoCanal())) }) { Text("Nuevo canal") }
        TextButton(onClick = {
            resultadoPrueba = "Enviando…"
            scope.launch {
                resultadoPrueba = if (Ntfy.enviar(Ntfy.prueba(a.ntfyCanal))) "Enviado: mira tu móvil" else "No se pudo enviar (¿hay internet?)"
            }
        }) { Text("Probar aviso") }
    }
    resultadoPrueba?.let {
        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = margenCampo)
    }
}
```

> Nota: "Nuevo canal" cambia el canal en el formulario; los suscriptores deben volver a suscribirse. "Probar aviso" usa el canal mostrado aunque aún no se haya pulsado Guardar.

- [ ] **Step 2: Incluir la sección en Ajustes** — en `AjustesAdminScreen.kt`:

Imports nuevos: `import com.fabri.nonna.pastillas.Pastillas`.

Estado nuevo, junto a `pinNuevo`:

```kotlin
    var textoManana by remember { mutableStateOf(Pastillas.formatearHora(ajustes.pastillaMinManana)) }
    var textoNoche by remember { mutableStateOf(Pastillas.formatearHora(ajustes.pastillaMinNoche)) }
    val horasOk = !a.pastillasActivas || horasValidas(textoManana, textoNoche)
```

En el `TextButton` de Guardar: cambiar `enabled = pinOk` por `enabled = pinOk && horasOk`, y antes de `onGuardar(final)` añadir:

```kotlin
                if (a.pastillasActivas) final = final.copy(
                    pastillaMinManana = Pastillas.parsearHora(textoManana)!!,
                    pastillaMinNoche = Pastillas.parsearHora(textoNoche)!!
                )
```

Insertar la sección justo antes del bloque `// ---------- Seguridad ----------`:

```kotlin
            SeccionPastillas(
                a, onCambio = { a = it },
                textoManana = textoManana, onTextoManana = { textoManana = it },
                textoNoche = textoNoche, onTextoNoche = { textoNoche = it }
            )

            HorizontalDivider(Modifier.padding(top = 4.dp))
```

- [ ] **Step 3: Comprobaciones** — en `Diagnostico.kt`, añadir imports:

```kotlin
import android.app.AlarmManager
import android.os.Build
import com.fabri.nonna.pastillas.AvisosPastillas
import com.fabri.nonna.pastillas.Ntfy
```

y antes del `familiares.forEach`:

```kotlin
        if (ajustes.pastillasActivas) {
            val am = ctx.getSystemService(AlarmManager::class.java)
            lista += Comprobacion(
                "Notificaciones de pastillas", AvisosPastillas.permitidas(ctx),
                "Ajustes → Apps → Inicio Nonna → Notificaciones → Permitir"
            )
            lista += Comprobacion(
                "Alarmas a la hora exacta", Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms(),
                "Ajustes → Apps → Inicio Nonna → Alarmas y recordatorios → Permitir"
            )
            lista += Comprobacion(
                "Aviso a la familia (ntfy)", Ntfy.canalValido(ajustes.ntfyCanal),
                "Ajustes → Pastillas → Nuevo canal, y suscríbete en la app ntfy"
            )
        }
```

- [ ] **Step 4: Acciones de simulación** — en `DiagnosticoScreen.kt`, añadir el parámetro `pastillasActivas: Boolean = false` al final de la firma de `DiagnosticoScreen`, los imports:

```kotlin
import androidx.compose.material.icons.filled.Notifications
import com.fabri.nonna.pastillas.ProgramadorPastillas
import com.fabri.nonna.pastillas.TipoEvento
import com.fabri.nonna.pastillas.Toma
```

y al final del `LazyColumn`, después de `items(pruebas) { ... }`:

```kotlin
            if (pastillasActivas) {
                item {
                    FilaAdmin("Simular aviso de la mañana", "Notificación y voz, como a las 8:50", Icons.Default.Notifications) {
                        ProgramadorPastillas.dispararAhora(ctx, TipoEvento.RECORDAR, Toma.MANANA)
                    }
                }
                item {
                    FilaAdmin("Simular comprobación de la mañana", "Si no está marcada, avisa por ntfy", Icons.Default.Notifications) {
                        ProgramadorPastillas.dispararAhora(ctx, TipoEvento.COMPROBAR, Toma.MANANA)
                    }
                }
            }
```

En `AppAbuela.kt`, en la llamada a `DiagnosticoScreen(...)`, pasar `pastillasActivas = ajustes.pastillasActivas`:

```kotlin
            DiagnosticoScreen(lista, controlador, familiares, onPermisos = { vuelta++ }, pastillasActivas = ajustes.pastillasActivas) { pantalla = Pantalla.Admin }
```

- [ ] **Step 5: Compilar y pasar todos los tests**

Run: `./gradlew testDebugUnitTest assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/fabri/nonna/ui/admin app/src/main/java/com/fabri/nonna/admin app/src/main/java/com/fabri/nonna/navegacion/AppAbuela.kt
git commit -m "feat(pastillas): sección en Ajustes, comprobaciones y simulación"
```

---

### Task 7: Verificación en el Redmi

**Files:** ninguno (solo si aparece un fallo; en ese caso, arreglarlo con su test y commit propio).

**Interfaces:** consume todo lo anterior.

- [ ] **Step 1: Instalar y abrir**

```bash
export PATH="$PATH:/c/Users/fabri/AppData/Local/Android/Sdk/platform-tools"
./gradlew installDebug && adb shell am start -n com.fabri.nonna/.MainActivity
```

Expected: `Installed on 1 device.` y la app en pantalla.

- [ ] **Step 2: Configurar** (Fabri, con el PIN): Admin → Ajustes → Pastillas → activar, permitir notificaciones, dejar 08:50 / 20:50, Guardar. Instalar ntfy en el móvil de Fabri y suscribirse al canal. Pulsar **Probar aviso**.

Expected: llega "✅ Aviso de prueba…" al móvil de Fabri.

- [ ] **Step 3: Alarma programada**

```bash
adb shell dumpsys alarm | grep -A3 "com.fabri.nonna"
```

Expected: una alarma `RTC_WAKEUP` / alarm clock de `com.fabri.nonna` a la hora del siguiente evento.

- [ ] **Step 4: Simular el aviso** — Comprobar botones → "Simular aviso de la mañana".

Expected: notificación "💊 Pastillas de la mañana" y la voz. En el log:

```bash
adb logcat -d -s NONNA_PASTILLAS:I
```

muestra `evento Evento(tipo=RECORDAR, toma=MANANA, …)`.

- [ ] **Step 5: Franja y marcar** (entre 08:50 y 20:50, o cambiando temporalmente la hora de la mañana a una ya pasada): la franja aparece en Inicio → tocarla → "¿Te has tomado…?" → SÍ.

Expected: voz "¡Muy bien, Nonna!", la franja desaparece, la notificación se quita.

- [ ] **Step 6: Comprobación sin marcar** — con una toma de hoy sin marcar, Comprobar botones → "Simular comprobación de la mañana".

Expected: llega a ntfy "💊 La Nonna no ha marcado las pastillas de la mañana". Si está marcada, no llega nada.

- [ ] **Step 7: Reinicio** — reiniciar el Redmi y repetir el Step 3.

Expected: la alarma sigue programada sin abrir la app. Si no aparece, comprobar que "Inicio automático" está activado para Inicio Nonna (MIUI bloquea `BOOT_COMPLETED` sin él) y anotarlo en `docs/guia-instalacion.md`.

- [ ] **Step 8: Dejar la hora de la mañana en 08:50** si se cambió en el Step 5, y commit de cualquier ajuste de documentación:

```bash
git add docs
git commit -m "docs: verificación de pastillas en el Redmi"
```
