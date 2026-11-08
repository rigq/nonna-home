# Launcher para la abuela — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Una app Android que sustituye la pantalla de inicio del Redmi de la abuela por 5 botones enormes. Cada botón lleva directamente a la acción final: videollamada o chat de WhatsApp con un familiar, mensajes nuevos, cámara, YouTube, ChatGPT por voz y el QR de la tarjeta DIA. Incluye un modo administrador protegido con PIN.

**Architecture:**
- **Ventana y navegación:** una única `MainActivity` declarada como HOME, con una navegación Compose propia basada en estado (sin librería de navegación).
- **Lógica pura en Kotlin, testeable en JVM sin Android:** `Lanzador` (qué intentar en cada acción, con plan B y C), `Telefono`, `Antirrebote`, `BandejaMensajes`, `OpenMeteo` y `Pin`.
- **Adaptadores finos de Android:** `Ejecutor` (crea los Intents), `WhatsAppContactos`, `LectorNotificaciones` y `Repositorio` (DataStore).

**Tech Stack:** Kotlin 2.x, Jetpack Compose (Material 3), Jetpack DataStore Preferences, kotlinx.serialization, ZXing core, JUnit 4.

**Spec:** `docs/superpowers/specs/2026-11-08-launcher-abuela-design.md`

## Cómo leer este plan si no sabes Android

- **Android Studio** es el editor. "Run ▶" compila e instala la app en el móvil conectado por USB.
- **Imports:** cuando un nombre salga en rojo en el código, pon el cursor encima y pulsa **Alt+Enter → Import**. En los archivos de lógica pura los imports van escritos; en los de Compose los añade Android Studio.
- **Ejecutar los tests unitarios:** desde la terminal de Android Studio (abajo, pestaña *Terminal*, que es PowerShell), en la raíz del proyecto:
  `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.<Clase>"`
- **Logcat** (pestaña abajo) muestra los mensajes de la app. Escribe `tag:SONDA` en el filtro para ver solo los de las sondas.
- **Proyecto:** se crea en `C:\Users\fabri\Desktop\LauncherAbuela`. Todas las rutas de este plan son relativas a esa carpeta.
- **Raíz del código:** `app/src/main/java/com/fabri/abuela/` se abrevia como `…/abuela/`, y `app/src/test/java/com/fabri/abuela/` como `test/…/abuela/`.

## Global Constraints

- Paquete de la app: `com.fabri.abuela` · minSdk **29** · solo vertical (`portrait`).
- Paquetes de terceros: WhatsApp `com.whatsapp`, YouTube `com.google.android.youtube`, ChatGPT `com.openai.chatgpt`, DIA → el que se descubra en la Tarea 2.
- Botones: alto mínimo **120dp**, separación **16dp**, títulos de **28sp** o más.
- Antirrebote: **1,5 s** tras cada acción.
- Modo administrador: **5 toques en el reloj en menos de 3 s** + PIN de **4 a 6 dígitos**. Tras **5 fallos**, **30 s** de espera. El PIN se guarda como SHA-256 con sal.
- Temperatura: Open-Meteo, se refresca si el dato tiene **30 minutos o más**. Sin red se muestra la caché; si nunca hubo datos, no se muestra nada.
- **Ningún dato sale del móvil.** Los mensajes solo viven en memoria. Las únicas llamadas de red son las de Open-Meteo.
- Videollamada: **siempre** pide confirmar con "¿Llamar a X?". Abrir un chat no pide confirmación.
- La abuela **nunca** ve un error técnico. Ve: "😕 No se ha podido abrir. Pídele ayuda a {nombreAyuda}." (por defecto "Fabri").
- Mensajes nuevos: **sin lectura en voz alta**. La voz solo anuncia acciones ("Abriendo la cámara") y se puede desactivar.
- Textos de la interfaz en español de España.

## Review Focus

1. **Teléfonos escritos de formas distintas** ("+34 612 345 678", "612-345-678", "0034612345678", el jid "34612345678@s.whatsapp.net"): todos deben reconocerse como el mismo número. Un número vacío nunca debe coincidir con nada. → Tests en la Tarea 5.
2. **Familiar con nombre vacío o foto borrada o corrupta:** se muestra un círculo con la inicial ("?" si no hay nombre) y nunca debe fallar. → Test de `inicial()` en la Tarea 9.
3. **WhatsApp vuelve a publicar la misma notificación** con más mensajes, publica un resumen de grupo, un "WhatsApp: 3 mensajes de 2 chats", o un título de grupo con "(3 mensajes)": no deben salir duplicados ni tarjetas basura. → Tests en la Tarea 11.
4. **Respuesta de Open-Meteo incompleta, vacía o con HTML de error, o sin red:** no debe fallar y se mantiene la caché. → Tests en la Tarea 15.
5. **Doble toque rápido en "SÍ, LLAMAR"**, o el reloj del sistema que retrocede: debe salir una sola llamada y ningún botón debe quedarse bloqueado para siempre. → Tests en la Tarea 4.

---

## Estructura de archivos

```
LauncherAbuela/
├── app/build.gradle.kts                      (T1 dependencias)
├── gradle/libs.versions.toml                 (T1 versiones)
├── app/src/main/AndroidManifest.xml          (T1, T12, T14)
├── docs/fase0-resultados.md                  (T2)
├── docs/guia-instalacion.md                  (T20)
└── app/src/main/java/com/fabri/abuela/
    ├── MainActivity.kt                       (T1, T8, T10, T12, T13, T15, T17, T18)
    ├── navegacion/Pantalla.kt                (T8 y siguientes)
    ├── navegacion/AppAbuela.kt               (T8 y siguientes)
    ├── navegacion/Controlador.kt             (T8)
    ├── ui/theme/Colores.kt, Medidas.kt, Tema.kt          (T3)
    ├── ui/components/BotonGrande.kt, BotonVolver.kt, FotoFamiliar.kt (T3, T10)
    ├── ui/home/HomeScreen.kt, Cabecera.kt, Fecha.kt      (T8, T15, T16)
    ├── ui/error/ErrorScreen.kt               (T8)
    ├── ui/familia/FamiliaScreen.kt, TarjetaFamiliar.kt, ConfirmarScreen.kt, SeccionMensajes.kt (T10, T13)
    ├── ui/dia/TarjetaDiaScreen.kt            (T14)
    ├── ui/admin/TecladoPin.kt, PinScreen.kt, AdminScreen.kt, AdminFamiliaresScreen.kt,
    │            EditarFamiliarScreen.kt, AjustesAdminScreen.kt, AsistenteScreen.kt, DiagnosticoScreen.kt (T17–T19)
    ├── acciones/Accion.kt, Destino.kt, Lanzador.kt, Paquetes.kt, Telefono.kt   (T5, T6)
    ├── acciones/Ejecutor.kt, WhatsAppContactos.kt        (T7)
    ├── datos/Modelos.kt, Codec.kt, Repositorio.kt        (T9)
    ├── mensajes/Modelos.kt, BandejaMensajes.kt, Asociacion.kt (T11)
    ├── mensajes/LectorNotificaciones.kt, Mensajes.kt     (T12)
    ├── dia/CodigoTarjeta.kt                  (T14)
    ├── tiempo/OpenMeteo.kt, ServicioTiempo.kt            (T15)
    ├── seguridad/Pin.kt                      (T16)
    ├── admin/Diagnostico.kt                  (T19)
    └── feedback/Antirrebote.kt, Voz.kt, Vibrar.kt        (T4)
```

---

## FASE 0 — Entorno y comprobación de riesgos

### Task 1: Crear el proyecto y fijarlo como pantalla de inicio (riesgo R4)

**Files:**
- Create: proyecto completo con el asistente de Android Studio
- Modify: `gradle/libs.versions.toml`, `build.gradle.kts` (raíz), `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `…/abuela/MainActivity.kt`

**Interfaces:**
- Produces: proyecto que compila, instalado en el Redmi como launcher predeterminado y con todas las dependencias disponibles.

- [ ] **Step 1: Instalar herramientas**
  - Descarga e instala **Android Studio** (developer.android.com/studio), con las opciones por defecto.
  - Instala **Git para Windows** si no lo tienes (git-scm.com).
  - Abre Android Studio y deja que descargue el SDK.

- [ ] **Step 2: Activar la depuración USB en el Redmi**
  1. Ajustes → Sobre el teléfono → toca 7 veces "Versión de HyperOS" (o "Versión MIUI") hasta que diga "Ya eres desarrollador".
  2. Ajustes → Ajustes adicionales → Opciones de desarrollador:
     - Activa **Depuración USB**.
     - Activa **Instalar vía USB**. Pide iniciar sesión con la cuenta Xiaomi.
     - Activa **Depuración USB (ajustes de seguridad)**.
  3. Conecta el móvil por cable al PC y acepta "¿Permitir depuración USB?" marcando "Permitir siempre".

- [ ] **Step 3: Crear el proyecto**
  En Android Studio: *New Project* → **Empty Activity** (la que usa Compose) →
  - Name: `LauncherAbuela`
  - Package name: `com.fabri.abuela`
  - Save location: `C:\Users\fabri\Desktop\LauncherAbuela`
  - Minimum SDK: **API 29 (Android 10)**
  - Build configuration language: Kotlin DSL
  → *Finish*. Espera a que termine "Gradle sync".

- [ ] **Step 4: Añadir versiones y librerías al catálogo**
  En `gradle/libs.versions.toml` añade estas líneas **dentro de las secciones que ya existen**, sin borrar nada:

```toml
[versions]
datastore = "1.1.1"
serializationJson = "1.7.3"
zxing = "3.5.3"

[libraries]
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serializationJson" }
zxing-core = { group = "com.google.zxing", name = "core", version.ref = "zxing" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```
  (La plantilla ya tiene una versión llamada `kotlin`. Si en tu archivo se llama distinto, por ejemplo `kotlinVersion`, usa ese nombre en `version.ref`.)

- [ ] **Step 5: Activar el plugin y las dependencias**
  En `build.gradle.kts` de la **raíz**, dentro de `plugins { }`, añade:
```kotlin
alias(libs.plugins.kotlin.serialization) apply false
```
  En `app/build.gradle.kts`, dentro de `plugins { }`, añade:
```kotlin
alias(libs.plugins.kotlin.serialization)
```
  y dentro de `dependencies { }`:
```kotlin
implementation(libs.androidx.datastore.preferences)
implementation(libs.kotlinx.serialization.json)
implementation(libs.zxing.core)
```
  Pulsa **Sync Now** (barra amarilla arriba). Debe terminar sin errores.

- [ ] **Step 6: Declarar la app como pantalla de inicio**
  Sustituye `app/src/main/AndroidManifest.xml` por:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <queries>
        <package android:name="com.whatsapp" />
        <package android:name="com.google.android.youtube" />
        <package android:name="com.openai.chatgpt" />
        <intent>
            <action android:name="android.media.action.STILL_IMAGE_CAMERA" />
        </intent>
        <intent>
            <action android:name="android.media.action.IMAGE_CAPTURE" />
        </intent>
    </queries>

    <application
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="Inicio Abuela"
        android:supportsRtl="true"
        android:theme="@style/Theme.LauncherAbuela">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTask"
            android:screenOrientation="portrait"
            android:stateNotNeeded="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```
  (`LAUNCHER` hace que la app también salga en el cajón de apps, lo que viene bien mientras se desarrolla.)

- [ ] **Step 7: Hola mundo**
  Sustituye `…/abuela/MainActivity.kt` por:
```kotlin
package com.fabri.abuela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Hola abuela 👋", fontSize = 40.sp)
            }
        }
    }
}
```

- [ ] **Step 8: Instalar y fijar como inicio en el Redmi**
  1. Arriba en Android Studio elige el Redmi en el desplegable de dispositivos y pulsa **Run ▶**. Debe aparecer "Hola abuela 👋".
  2. En el Redmi: Ajustes → Pantalla de inicio → **Navegación del sistema → Botones** (los 3 botones ◁ ○ □).
  3. Ajustes → Apps → Administrar apps → (⋮) **Apps predeterminadas → Pantalla de inicio → "Inicio Abuela"**.
  4. Pulsa ○: debe salir "Hola abuela 👋".
  5. Abre cualquier app y pulsa ○: debe volver a "Hola abuela 👋".

  **Si HyperOS no deja elegirla** (R4): apunta qué pasa y la versión de HyperOS en `docs/fase0-resultados.md` (Tarea 2) antes de seguir. Para volver al launcher normal, en ese mismo menú elige "Escritorio del sistema".

- [ ] **Step 9: Crear el repositorio git y el primer commit**
  En la terminal de Android Studio:
```powershell
git init
git add .
git commit -m "chore: proyecto base declarado como launcher"
```

---

### Task 2: Sondas de riesgo R1, R2, R3, paquete DIA

**Files:**
- Create: `…/abuela/Sondas.kt` (temporal, se borra en la Tarea 8)
- Modify: `…/abuela/MainActivity.kt` (temporal)
- Create: `docs/fase0-resultados.md`

**Interfaces:**
- Produces, en `docs/fase0-resultados.md`, cuatro datos que usan tareas posteriores:
  - (a) palabras del atajo de voz de ChatGPT → Tarea 6, `Lanzador`
  - (b) formato de `DATA1` en las filas de videollamada de WhatsApp → Tarea 7
  - (c) si el QR de DIA es fijo o dinámico, y si es QR o código de barras → Tarea 14
  - (d) nombre de paquete de la app DIA → Tarea 14

- [ ] **Step 1: Escribir las sondas**
  `…/abuela/Sondas.kt`:
```kotlin
package com.fabri.abuela

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Process
import android.provider.ContactsContract
import android.util.Log

object Sondas {
    private const val TAG = "SONDA"

    fun atajosChatGpt(ctx: Context) {
        val la = ctx.getSystemService(LauncherApps::class.java)
        Log.i(TAG, "¿Somos launcher? ${la.hasShortcutHostPermission()}")
        if (!la.hasShortcutHostPermission()) return
        val q = LauncherApps.ShortcutQuery()
            .setPackage("com.openai.chatgpt")
            .setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
            )
        val lista = la.getShortcuts(q, Process.myUserHandle()).orEmpty()
        if (lista.isEmpty()) Log.i(TAG, "ChatGPT: sin atajos")
        lista.forEach { Log.i(TAG, "ChatGPT atajo id=${it.id} corto='${it.shortLabel}' largo='${it.longLabel}'") }
    }

    /** Devuelve el _ID de la primera fila de videollamada encontrada (para probarla). */
    fun filasWhatsApp(ctx: Context): Long? {
        var primeraVideo: Long? = null
        ctx.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(
                ContactsContract.Data._ID, ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA1, ContactsContract.Data.DATA2,
                ContactsContract.Data.DATA3, ContactsContract.Data.DISPLAY_NAME
            ),
            "${ContactsContract.Data.MIMETYPE} LIKE ?",
            arrayOf("vnd.android.cursor.item/vnd.com.whatsapp%"),
            null
        )?.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(0); val mime = c.getString(1)
                Log.i(TAG, "WA fila id=$id mime=$mime data1=${c.getString(2)} data2=${c.getString(3)} data3=${c.getString(4)} nombre=${c.getString(5)}")
                if (primeraVideo == null && mime.endsWith("video.call")) primeraVideo = id
            }
        }
        Log.i(TAG, "Primera fila de videollamada: $primeraVideo")
        return primeraVideo
    }

    fun probarVideollamada(ctx: Context, filaId: Long) {
        val uri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, filaId.toString())
        val i = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "vnd.android.cursor.item/vnd.com.whatsapp.video.call")
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { ctx.startActivity(i); Log.i(TAG, "Videollamada lanzada") }
        catch (e: Exception) { Log.e(TAG, "Videollamada FALLÓ", e) }
    }
}
```

- [ ] **Step 2: Botones temporales en MainActivity**
  Sustituye el `setContent { … }` de `MainActivity.onCreate` por:
```kotlin
        requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), 1)
        setContent {
            androidx.compose.foundation.layout.Column(
                Modifier.fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Button(onClick = { Sondas.atajosChatGpt(this@MainActivity) }) { Text("Sonda ChatGPT", fontSize = 24.sp) }
                androidx.compose.material3.Button(onClick = { Sondas.filasWhatsApp(this@MainActivity) }) { Text("Sonda WhatsApp", fontSize = 24.sp) }
                androidx.compose.material3.Button(onClick = {
                    Sondas.filasWhatsApp(this@MainActivity)?.let { Sondas.probarVideollamada(this@MainActivity, it) }
                }) { Text("Probar videollamada", fontSize = 24.sp) }
            }
        }
```
  Importa `androidx.compose.ui.unit.dp` con Alt+Enter.

- [ ] **Step 3: Ejecutar la sonda de ChatGPT (R2)**
  - Run ▶ y acepta el permiso de contactos.
  - Asegúrate de que la app sigue como pantalla de inicio predeterminada; si no, `hasShortcutHostPermission` es `false`.
  - Pulsa "Sonda ChatGPT" y mira Logcat con el filtro `tag:SONDA`.
  - **Esperado:** "¿Somos launcher? true" y una lista de atajos.
  - Apunta el `id` y las etiquetas del atajo de voz, por ejemplo "Voice" o "Voz".

- [ ] **Step 4: Ejecutar la sonda de WhatsApp (R1)**
  - El móvil necesita al menos un contacto que tenga WhatsApp. Usa tu propio número guardado en su agenda.
  - Pulsa "Sonda WhatsApp".
  - **Esperado:** filas con `mime=…video.call` y `voip.call`.
  - Apunta cómo es `data1`. Lo más probable es `34XXXXXXXXX@s.whatsapp.net`.
  - Después pulsa "Probar videollamada". **Ojo: llama de verdad al primer contacto de la lista.** Hazlo con tu número y ten tu otro móvil a mano.
  - **Esperado:** WhatsApp abre directamente la videollamada.

- [ ] **Step 5: Paquete de DIA (d)**
  En la terminal de Android Studio:
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell pm list packages | Select-String -Pattern "dia"
```
  Apunta el paquete de la app DIA, que es la línea `package:…` que corresponde a DIA.

- [ ] **Step 6: Tarjeta DIA fija o dinámica (R3)**
  1. Abre la app DIA en su pantalla de tarjeta o Club DIA. Fíjate si muestra un **QR** o un **código de barras** y si aparece un número de tarjeta.
  2. Escanea el código con otro móvil (cámara o Google Lens) y apunta el contenido.
  3. Cierra la app, vuelve a abrirla al cabo de unas horas y escanéalo otra vez.
  4. Si el contenido es **siempre igual**, el código es fijo y el tipo será `QR` o `BARRAS`. Si **cambia**, es dinámico y el tipo será `APP`.

- [ ] **Step 7: Anotar resultados**
  Crea `docs/fase0-resultados.md`:
```markdown
# Resultados fase 0 (fecha: AAAA-MM-DD)

- HyperOS versión: …
- R4 launcher predeterminado: OK / problema: …
- R2 atajo voz ChatGPT: id='…' corto='…' largo='…'  → palabras para buscar: ["…", "…"]
- R1 videollamada WhatsApp: OK / FALLA. Formato data1: '…'
- (d) paquete DIA: …
- R3 tarjeta DIA: tipo = QR | BARRAS | APP. Contenido escaneado: (no lo pegues si es tu número real; basta con "fijo, 13 dígitos")
```

- [ ] **Step 8: Commit**
```powershell
git add .
git commit -m "chore: sondas de fase 0 y resultados"
```

> **Decisión:** si R1 falla, la Tarea 7 se hace igual: el plan B (abrir el chat) ya está previsto en el `Lanzador`. Si R2 no encuentra ningún atajo de voz, en la Tarea 6 se dejan las palabras `["voice", "voz"]` y se usará el plan B (abrir ChatGPT normal).

---

## FASE 1 — Esqueleto

### Task 3: Tema de alto contraste y botones base

**Files:**
- Create: `…/abuela/ui/theme/Colores.kt`, `…/abuela/ui/theme/Medidas.kt`, `…/abuela/ui/theme/Tema.kt`
- Create: `…/abuela/ui/components/BotonGrande.kt`, `…/abuela/ui/components/BotonVolver.kt`
- Delete: los archivos de tema que generó la plantilla en `…/abuela/ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`)

**Interfaces:**
- Produces:
  - `object Colores { fondo, texto, textoSobreBoton, familia, camara, youtube, chatgpt, dia, whatsapp, si, no, volver, mensaje, aviso: Color }`
  - `object Medidas { botonMin: Dp, separacion: Dp, titulo: TextUnit, subtitulo: TextUnit, texto: TextUnit }`
  - `@Composable fun TemaAbuela(content: @Composable () -> Unit)`
  - `@Composable fun BotonGrande(emoji: String, titulo: String, subtitulo: String?, color: Color, modifier: Modifier = Modifier, contentColor: Color = Colores.textoSobreBoton, onClick: () -> Unit)`
  - `@Composable fun BotonVolver(onClick: () -> Unit)`

- [ ] **Step 1: Borrar el tema de la plantilla**
  Borra `Color.kt`, `Theme.kt` y `Type.kt` de `ui/theme/` (clic derecho → Delete). En el Paso 6 se quitan las referencias a ellos.

- [ ] **Step 2: Colores y medidas**
  `…/abuela/ui/theme/Colores.kt`:
```kotlin
package com.fabri.abuela.ui.theme

import androidx.compose.ui.graphics.Color

object Colores {
    val fondo = Color(0xFFFFFFFF)
    val texto = Color(0xFF111111)
    val textoSobreBoton = Color(0xFF111111)
    val familia = Color(0xFFFFD54F)
    val camara = Color(0xFF81D4FA)
    val youtube = Color(0xFFEF9A9A)
    val chatgpt = Color(0xFFA5D6A7)
    val dia = Color(0xFFFFAB91)
    val whatsapp = Color(0xFFC8E6C9)
    val mensaje = Color(0xFFE3F2FD)
    val si = Color(0xFF2E7D32)
    val no = Color(0xFFC62828)
    val volver = Color(0xFFE0E0E0)
    val aviso = Color(0xFFD32F2F)
}
```
  `…/abuela/ui/theme/Medidas.kt`:
```kotlin
package com.fabri.abuela.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Medidas {
    val botonMin = 120.dp
    val separacion = 16.dp
    val titulo = 28.sp
    val subtitulo = 20.sp
    val texto = 24.sp
}
```

- [ ] **Step 3: Tema**
  `…/abuela/ui/theme/Tema.kt`:
```kotlin
package com.fabri.abuela.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun TemaAbuela(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Colores.fondo, surface = Colores.fondo,
            onBackground = Colores.texto, onSurface = Colores.texto
        ),
        content = content
    )
}
```

- [ ] **Step 4: Botón grande**
  `…/abuela/ui/components/BotonGrande.kt`:
```kotlin
package com.fabri.abuela.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.abuela.ui.theme.Colores
import com.fabri.abuela.ui.theme.Medidas

@Composable
fun BotonGrande(
    emoji: String,
    titulo: String,
    subtitulo: String?,
    color: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = Colores.textoSobreBoton,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = Medidas.botonMin),
        shape = RoundedCornerShape(24.dp),
        color = color,
        contentColor = contentColor,
        shadowElevation = 4.dp
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (emoji.isNotEmpty()) Text(emoji, fontSize = 44.sp)
            Text(titulo, fontSize = Medidas.titulo, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            if (subtitulo != null) Text(subtitulo, fontSize = Medidas.subtitulo, textAlign = TextAlign.Center)
        }
    }
}
```
  `…/abuela/ui/components/BotonVolver.kt`:
```kotlin
package com.fabri.abuela.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabri.abuela.ui.theme.Colores

@Composable
fun BotonVolver(onClick: () -> Unit) {
    BotonGrande(
        emoji = "", titulo = "◀  VOLVER AL INICIO", subtitulo = null,
        color = Colores.volver, modifier = Modifier.fillMaxWidth().height(120.dp), onClick = onClick
    )
}
```

- [ ] **Step 5: Vista previa**
  Al final de `BotonGrande.kt` añade:
```kotlin
@androidx.compose.ui.tooling.preview.Preview(widthDp = 200, heightDp = 200)
@Composable
private fun VistaBoton() {
    BotonGrande("📷", "CÁMARA", "Hacer fotos", Colores.camara) {}
}
```
  Abre el archivo y pulsa **Split** (arriba a la derecha). Debe verse un botón azul claro con 📷, "CÁMARA" en negrita y "Hacer fotos".

- [ ] **Step 6: Compilar**
  En `MainActivity.kt` quita cualquier referencia a `LauncherAbuelaTheme` que dejara la plantilla. Si no hay ninguna, no hace falta tocarlo.
  Run:
```powershell
.\gradlew.bat assembleDebug
```
  Esperado: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**
```powershell
git add .
git commit -m "feat: tema de alto contraste y botones grandes"
```

---

### Task 4: Antirrebote, voz y vibración

**Files:**
- Create: `…/abuela/feedback/Antirrebote.kt`, `…/abuela/feedback/Voz.kt`, `…/abuela/feedback/Vibrar.kt`
- Test: `test/…/abuela/feedback/AntirreboteTest.kt`

**Interfaces:**
- Produces:
  - `class Antirrebote(ventanaMs: Long = 1500, reloj: () -> Long) { fun intentar(): Boolean }`
  - `class Voz(context: Context) { var activada: Boolean; fun decir(texto: String); fun cerrar() }`
  - `fun vibrarCorto(context: Context)`

- [ ] **Step 1: Escribir los tests (incluye Review Focus 5)**
  `test/…/abuela/feedback/AntirreboteTest.kt`:
```kotlin
package com.fabri.abuela.feedback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AntirreboteTest {
    private var ahora = 10_000L
    private val a = Antirrebote(ventanaMs = 1500, reloj = { ahora })

    @Test fun primerToquePasa() = assertTrue(a.intentar())

    @Test fun dobleToqueRapidoSeIgnora() {
        assertTrue(a.intentar())
        ahora += 200
        assertFalse(a.intentar())   // doble toque en "SÍ, LLAMAR" → una sola llamada
    }

    @Test fun pasadaLaVentanaVuelveAPasar() {
        assertTrue(a.intentar())
        ahora += 1500
        assertTrue(a.intentar())
    }

    @Test fun toqueIgnoradoNoAlargaLaVentana() {
        assertTrue(a.intentar())
        ahora += 1000; assertFalse(a.intentar())
        ahora += 500; assertTrue(a.intentar())
    }

    @Test fun relojQueRetrocedeNoBloqueaParaSiempre() {
        assertTrue(a.intentar())
        ahora -= 60_000
        assertTrue(a.intentar())
    }
}
```

- [ ] **Step 2: Comprobar que falla**
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.feedback.AntirreboteTest"`
  Expected: FAIL de compilación con "Unresolved reference: Antirrebote".

- [ ] **Step 3: Implementar el antirrebote**
  `…/abuela/feedback/Antirrebote.kt`:
```kotlin
package com.fabri.abuela.feedback

import android.os.SystemClock

/** Deja pasar un toque y bloquea los siguientes durante [ventanaMs]. */
class Antirrebote(
    private val ventanaMs: Long = 1500,
    private val reloj: () -> Long = { SystemClock.elapsedRealtime() }
) {
    private var ultimo: Long? = null

    @Synchronized
    fun intentar(): Boolean {
        val ahora = reloj()
        val u = ultimo
        if (u != null && ahora >= u && ahora - u < ventanaMs) return false
        ultimo = ahora
        return true
    }
}
```

- [ ] **Step 4: Comprobar que pasa**
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.feedback.AntirreboteTest"`
  Expected: `BUILD SUCCESSFUL`, 5 tests OK.

- [ ] **Step 5: Voz y vibración**
  `…/abuela/feedback/Voz.kt`:
```kotlin
package com.fabri.abuela.feedback

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class Voz(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var lista = false
    var activada = true

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("es", "ES")
            lista = true
        }
    }

    fun decir(texto: String) {
        if (activada && lista) tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "abuela")
    }

    fun cerrar() = tts.shutdown()
}
```
  `…/abuela/feedback/Vibrar.kt`:
```kotlin
package com.fabri.abuela.feedback

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

fun vibrarCorto(context: Context) {
    val v = context.getSystemService(Vibrator::class.java) ?: return
    if (v.hasVibrator()) v.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
}
```

- [ ] **Step 6: Commit**
```powershell
git add .
git commit -m "feat: antirrebote, voz y vibración"
```

---

### Task 5: Normalizar teléfonos

**Files:**
- Create: `…/abuela/acciones/Telefono.kt`
- Test: `test/…/abuela/acciones/TelefonoTest.kt`

**Interfaces:**
- Produces:
  - `object Telefono { fun normalizar(t: String, prefijo: String = "34"): String; fun mismo(a: String, b: String): Boolean; fun paraWaMe(t: String): String }`
  - `normalizar` devuelve solo dígitos, con prefijo de país y sin `+` ni `00`. Si no queda ningún dígito, devuelve `""`.

- [ ] **Step 1: Escribir los tests (Review Focus 1)**
  `test/…/abuela/acciones/TelefonoTest.kt`:
```kotlin
package com.fabri.abuela.acciones

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TelefonoTest {
    @Test fun formatosEspanolesSonElMismo() {
        val esperado = "34612345678"
        assertEquals(esperado, Telefono.normalizar("+34 612 345 678"))
        assertEquals(esperado, Telefono.normalizar("612-345-678"))
        assertEquals(esperado, Telefono.normalizar("0034612345678"))
        assertEquals(esperado, Telefono.normalizar("34612345678"))
        assertEquals(esperado, Telefono.normalizar("(+34) 612.345.678"))
    }

    @Test fun jidDeWhatsApp() =
        assertEquals("34612345678", Telefono.normalizar("34612345678@s.whatsapp.net".substringBefore("@")))

    @Test fun vacioNoCoincideConNada() {
        assertEquals("", Telefono.normalizar(""))
        assertEquals("", Telefono.normalizar("abc"))
        assertFalse(Telefono.mismo("", ""))
        assertFalse(Telefono.mismo("abc", "+34 612 345 678"))
    }

    @Test fun mismoNumeroEnFormatosDistintos() =
        assertTrue(Telefono.mismo("612 345 678", "+34612345678"))

    @Test fun waMeSinMas() = assertEquals("34612345678", Telefono.paraWaMe("+34 612 345 678"))
}
```

- [ ] **Step 2: Comprobar que falla**
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.acciones.TelefonoTest"`
  Expected: FAIL "Unresolved reference: Telefono".

- [ ] **Step 3: Implementar**
  `…/abuela/acciones/Telefono.kt`:
```kotlin
package com.fabri.abuela.acciones

object Telefono {
    /** Solo dígitos con prefijo de país. "" si no hay número. */
    fun normalizar(t: String, prefijo: String = "34"): String {
        val limpio = t.filter { it.isDigit() || it == '+' }
        val sinPrefijoInternacional = when {
            limpio.startsWith("+") -> limpio.drop(1)
            limpio.startsWith("00") -> limpio.drop(2)
            else -> limpio
        }.filter(Char::isDigit)
        if (sinPrefijoInternacional.isEmpty()) return ""
        val esNacional = !limpio.startsWith("+") && !limpio.startsWith("00") && sinPrefijoInternacional.length == 9
        return if (esNacional) prefijo + sinPrefijoInternacional else sinPrefijoInternacional
    }

    fun mismo(a: String, b: String): Boolean {
        val na = normalizar(a)
        return na.isNotEmpty() && na == normalizar(b)
    }

    fun paraWaMe(t: String): String = normalizar(t)
}
```

- [ ] **Step 4: Comprobar que pasa**
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.acciones.TelefonoTest"`
  Expected: PASS (5 tests).

- [ ] **Step 5: Commit**
```powershell
git add .
git commit -m "feat: normalización de teléfonos"
```

---

### Task 6: Acciones y Lanzador (plan A, B y C)

**Files:**
- Create: `…/abuela/acciones/Paquetes.kt`, `…/abuela/acciones/Accion.kt`, `…/abuela/acciones/Destino.kt`, `…/abuela/acciones/Lanzador.kt`
- Test: `test/…/abuela/acciones/LanzadorTest.kt`

**Interfaces:**
- Consumes: `Telefono.paraWaMe(t)`
- Produces:
  - `sealed interface Accion` con `Camara`, `YouTube`, `ChatGptVoz`, `AbrirWhatsApp`, `WhatsAppChat(telefono)`, `WhatsAppVideo(telefono)`. Las Tareas 13 y 14 añaden más.
  - `sealed interface Destino` con `Ver(uri, paquete?, mime?)`, `AccionSistema(action)`, `CamaraPorDefecto`, `AbrirApp(paquete)`, `Atajo(paquete, palabras)`, `VideoWhatsApp(telefono)`.
  - `enum class Resultado { Ok, PlanB, Error }`
  - `class Lanzador(abrir: (Destino) -> Boolean) { fun planes(a: Accion): List<Destino>; fun ejecutar(a: Accion): Resultado }`

- [ ] **Step 1: Escribir los tests**
  `test/…/abuela/acciones/LanzadorTest.kt`:
```kotlin
package com.fabri.abuela.acciones

import org.junit.Assert.assertEquals
import org.junit.Test

class LanzadorTest {
    private val intentados = mutableListOf<Destino>()
    private fun lanzadorQueAcepta(vararg ok: Destino) = Lanzador { d -> intentados += d; d in ok }

    @Test fun camaraPlanA() {
        val l = lanzadorQueAcepta(Destino.AccionSistema(Lanzador.ACCION_CAMARA_FOTO))
        assertEquals(Resultado.Ok, l.ejecutar(Accion.Camara))
    }

    @Test fun camaraCaeAPlanB() {
        val l = lanzadorQueAcepta(Destino.CamaraPorDefecto)
        assertEquals(Resultado.PlanB, l.ejecutar(Accion.Camara))
        assertEquals(2, intentados.size)
    }

    @Test fun youtubeNoInstaladoEsError() {
        val l = lanzadorQueAcepta()
        assertEquals(Resultado.Error, l.ejecutar(Accion.YouTube))
    }

    @Test fun chatGptSinAtajoAbreLaApp() {
        val l = lanzadorQueAcepta(Destino.AbrirApp(Paquetes.CHATGPT))
        assertEquals(Resultado.PlanB, l.ejecutar(Accion.ChatGptVoz))
        assertEquals(Destino.Atajo(Paquetes.CHATGPT, Lanzador.PALABRAS_VOZ), intentados.first())
    }

    @Test fun chatDeWhatsAppUsaWaMe() {
        val planes = Lanzador { false }.planes(Accion.WhatsAppChat("+34 612 345 678"))
        assertEquals(Destino.Ver("https://wa.me/34612345678", Paquetes.WHATSAPP), planes.first())
        assertEquals(Destino.AbrirApp(Paquetes.WHATSAPP), planes.last())
    }

    @Test fun videollamadaCaeAlChat() {
        val chat = Destino.Ver("https://wa.me/34612345678", Paquetes.WHATSAPP)
        val l = lanzadorQueAcepta(chat)
        assertEquals(Resultado.PlanB, l.ejecutar(Accion.WhatsAppVideo("612345678")))
    }

    @Test fun excepcionCuentaComoFallo() {
        val l = Lanzador { d -> if (d is Destino.AccionSistema) throw SecurityException() else true }
        assertEquals(Resultado.PlanB, l.ejecutar(Accion.Camara))
    }

    @Test fun telefonoVacioNoIntentaWaMe() {
        val planes = Lanzador { false }.planes(Accion.WhatsAppChat(""))
        assertEquals(listOf(Destino.AbrirApp(Paquetes.WHATSAPP)), planes)
    }
}
```

- [ ] **Step 2: Comprobar que falla**
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.acciones.LanzadorTest"`
  Expected: FAIL "Unresolved reference: Lanzador".

- [ ] **Step 3: Implementar**
  `…/abuela/acciones/Paquetes.kt`:
```kotlin
package com.fabri.abuela.acciones

object Paquetes {
    const val WHATSAPP = "com.whatsapp"
    const val YOUTUBE = "com.google.android.youtube"
    const val CHATGPT = "com.openai.chatgpt"
}
```
  `…/abuela/acciones/Accion.kt`:
```kotlin
package com.fabri.abuela.acciones

sealed interface Accion {
    data object Camara : Accion
    data object YouTube : Accion
    data object ChatGptVoz : Accion
    data object AbrirWhatsApp : Accion
    data class WhatsAppChat(val telefono: String) : Accion
    data class WhatsAppVideo(val telefono: String) : Accion
}
```
  `…/abuela/acciones/Destino.kt`:
```kotlin
package com.fabri.abuela.acciones

/** Descripción pura de "qué abrir". El Ejecutor (Tarea 7) la convierte en Intent. */
sealed interface Destino {
    data class Ver(val uri: String, val paquete: String? = null, val mime: String? = null) : Destino
    data class AccionSistema(val action: String) : Destino
    data object CamaraPorDefecto : Destino
    data class AbrirApp(val paquete: String) : Destino
    data class Atajo(val paquete: String, val palabras: List<String>) : Destino
    data class VideoWhatsApp(val telefono: String) : Destino
}
```
  `…/abuela/acciones/Lanzador.kt` (en `PALABRAS_VOZ` pon las palabras que apuntaste en `docs/fase0-resultados.md`, siempre en minúsculas):
```kotlin
package com.fabri.abuela.acciones

enum class Resultado { Ok, PlanB, Error }

class Lanzador(private val abrir: (Destino) -> Boolean) {

    companion object {
        const val ACCION_CAMARA_FOTO = "android.media.action.STILL_IMAGE_CAMERA"
        val PALABRAS_VOZ = listOf("voice", "voz")
    }

    fun planes(a: Accion): List<Destino> = when (a) {
        Accion.Camara -> listOf(Destino.AccionSistema(ACCION_CAMARA_FOTO), Destino.CamaraPorDefecto)
        Accion.YouTube -> listOf(Destino.AbrirApp(Paquetes.YOUTUBE))
        Accion.ChatGptVoz -> listOf(Destino.Atajo(Paquetes.CHATGPT, PALABRAS_VOZ), Destino.AbrirApp(Paquetes.CHATGPT))
        Accion.AbrirWhatsApp -> listOf(Destino.AbrirApp(Paquetes.WHATSAPP))
        is Accion.WhatsAppChat -> chat(a.telefono) + Destino.AbrirApp(Paquetes.WHATSAPP)
        is Accion.WhatsAppVideo -> listOfNotNull(
            Destino.VideoWhatsApp(a.telefono).takeIf { Telefono.normalizar(a.telefono).isNotEmpty() }
        ) + chat(a.telefono) + Destino.AbrirApp(Paquetes.WHATSAPP)
    }

    private fun chat(tel: String): List<Destino> {
        val n = Telefono.paraWaMe(tel)
        return if (n.isEmpty()) emptyList() else listOf(Destino.Ver("https://wa.me/$n", Paquetes.WHATSAPP))
    }

    fun ejecutar(a: Accion): Resultado {
        planes(a).forEachIndexed { i, d ->
            val ok = try { abrir(d) } catch (e: Exception) { false }
            if (ok) return if (i == 0) Resultado.Ok else Resultado.PlanB
        }
        return Resultado.Error
    }
}
```

- [ ] **Step 4: Comprobar que pasa**
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.acciones.LanzadorTest"`
  Expected: PASS (8 tests).

- [ ] **Step 5: Commit**
```powershell
git add .
git commit -m "feat: lanzador de acciones con planes B y C"
```

---

### Task 7: Ejecutor de Android y videollamada de WhatsApp

**Files:**
- Create: `…/abuela/acciones/WhatsAppContactos.kt`, `…/abuela/acciones/Ejecutor.kt`

**Interfaces:**
- Consumes: `Destino`, `Telefono`, `Paquetes`
- Produces:
  - `class Ejecutor(activity: Activity) { fun abrir(d: Destino): Boolean }`
  - `object WhatsAppContactos { const val MIME_VIDEO; fun tienePermiso(ctx): Boolean; fun buscarVideollamada(ctx, telefono): Uri?; fun nombreEnAgenda(ctx, telefono): String? }`

- [ ] **Step 1: WhatsAppContactos**
  Si en la fase 0 `data1` **no** era del tipo `34…@s.whatsapp.net`, cambia la línea marcada con `// FORMATO` para que extraiga el número del formato que apuntaste.
  `…/abuela/acciones/WhatsAppContactos.kt`:
```kotlin
package com.fabri.abuela.acciones

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract

object WhatsAppContactos {
    const val MIME_VIDEO = "vnd.android.cursor.item/vnd.com.whatsapp.video.call"

    fun tienePermiso(ctx: Context) =
        ctx.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    fun buscarVideollamada(ctx: Context, telefono: String): Uri? {
        if (!tienePermiso(ctx)) return null
        val objetivo = Telefono.normalizar(telefono)
        if (objetivo.isEmpty()) return null
        ctx.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Data._ID, ContactsContract.Data.DATA1),
            "${ContactsContract.Data.MIMETYPE} = ?", arrayOf(MIME_VIDEO), null
        )?.use { c ->
            while (c.moveToNext()) {
                val data1 = c.getString(1) ?: continue
                val numero = data1.substringBefore("@") // FORMATO
                if (Telefono.normalizar(numero) == objetivo) {
                    return ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, c.getLong(0))
                }
            }
        }
        return null
    }

    fun nombreEnAgenda(ctx: Context, telefono: String): String? {
        if (!tienePermiso(ctx) || Telefono.normalizar(telefono).isEmpty()) return null
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(telefono))
        return try {
            ctx.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
                ?.use { if (it.moveToFirst()) it.getString(0) else null }
        } catch (e: Exception) { null }
    }
}
```

- [ ] **Step 2: Ejecutor**
  `…/abuela/acciones/Ejecutor.kt`:
```kotlin
package com.fabri.abuela.acciones

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Process
import android.provider.MediaStore

class Ejecutor(private val activity: Activity) {

    fun abrir(d: Destino): Boolean = when (d) {
        is Destino.Ver -> lanzar(Intent(Intent.ACTION_VIEW).apply {
            if (d.mime != null) setDataAndType(Uri.parse(d.uri), d.mime) else data = Uri.parse(d.uri)
            d.paquete?.let { setPackage(it) }
        })
        is Destino.AccionSistema -> lanzar(Intent(d.action))
        Destino.CamaraPorDefecto -> {
            val paquete = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .resolveActivity(activity.packageManager)?.packageName
            paquete?.let { abrir(Destino.AbrirApp(it)) } ?: false
        }
        is Destino.AbrirApp ->
            activity.packageManager.getLaunchIntentForPackage(d.paquete)?.let { lanzar(it) } ?: false
        is Destino.Atajo -> lanzarAtajo(d)
        is Destino.VideoWhatsApp ->
            WhatsAppContactos.buscarVideollamada(activity, d.telefono)?.let { uri ->
                lanzar(Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, WhatsAppContactos.MIME_VIDEO)
                    setPackage(Paquetes.WHATSAPP)
                })
            } ?: false
    }

    private fun lanzar(i: Intent): Boolean = try {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(i)
        true
    } catch (e: ActivityNotFoundException) { false } catch (e: SecurityException) { false }

    private fun lanzarAtajo(d: Destino.Atajo): Boolean {
        val la = activity.getSystemService(LauncherApps::class.java) ?: return false
        if (!la.hasShortcutHostPermission()) return false
        val q = LauncherApps.ShortcutQuery().setPackage(d.paquete).setQueryFlags(
            LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
        )
        val lista = try { la.getShortcuts(q, Process.myUserHandle()) } catch (e: Exception) { null } ?: return false
        val atajo = lista.firstOrNull { s ->
            val t = "${s.id} ${s.shortLabel} ${s.longLabel}".lowercase()
            d.palabras.any { it in t }
        } ?: return false
        return try { la.startShortcut(atajo, null, null); true } catch (e: Exception) { false }
    }
}
```

- [ ] **Step 3: Compilar**
  Run: `.\gradlew.bat assembleDebug`
  Expected: `BUILD SUCCESSFUL`. Se prueba en el móvil en la Tarea 8.

- [ ] **Step 4: Commit**
```powershell
git add .
git commit -m "feat: ejecutor de intents, atajos y videollamada de WhatsApp"
```

---

### Task 8: Pantalla de inicio, navegación y pantalla de error

**Files:**
- Create: `…/abuela/navegacion/Pantalla.kt`, `…/abuela/navegacion/Controlador.kt`, `…/abuela/navegacion/AppAbuela.kt`
- Create: `…/abuela/ui/home/Fecha.kt`, `…/abuela/ui/home/Cabecera.kt`, `…/abuela/ui/home/HomeScreen.kt`, `…/abuela/ui/error/ErrorScreen.kt`
- Modify: `…/abuela/MainActivity.kt` (se reescribe entero)
- Delete: `…/abuela/Sondas.kt`
- Test: `test/…/abuela/ui/home/FechaTest.kt`

**Interfaces:**
- Consumes: `Lanzador`, `Ejecutor`, `Antirrebote`, `Voz`, `vibrarCorto`, `BotonGrande`, `BotonVolver`, `Colores`, `TemaAbuela`
- Produces:
  - `sealed interface Pantalla { Inicio, Familia, TarjetaDia, Error }`. Las tareas siguientes añaden más.
  - `class Controlador(lanzador, antirrebote, voz, vibrar: () -> Unit) { val errores: SharedFlow<Unit>; fun permitir(frase: String?): Boolean; fun pulsar(a: Accion, frase: String) }`
  - `fun fechaLarga(d: LocalDate): String`
  - `@Composable fun Cabecera(temperatura: String? = null, onToqueReloj: () -> Unit = {})`
  - `@Composable fun HomeScreen(controlador: Controlador, temperatura: String?, onToqueReloj: () -> Unit, onFamilia: () -> Unit, onDia: () -> Unit)`
  - `@Composable fun ErrorScreen(nombreAyuda: String, onVolver: () -> Unit)`
  - `@Composable fun AppAbuela(controlador: Controlador, irAInicio: Int)`
  - `MainActivity` expone `controlador` e `irAInicio`.

- [ ] **Step 1: Test de la fecha**
  `test/…/abuela/ui/home/FechaTest.kt`:
```kotlin
package com.fabri.abuela.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FechaTest {
    @Test fun domingoEnEspanol() =
        assertEquals("Domingo, 8 de noviembre", fechaLarga(LocalDate.of(2026, 11, 8)))
    @Test fun miercoles() =
        assertEquals("Miércoles, 1 de abril", fechaLarga(LocalDate.of(2026, 4, 1)))
}
```
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.ui.home.FechaTest"` → Expected: FAIL "Unresolved reference: fechaLarga".

- [ ] **Step 2: Implementar la fecha**
  `…/abuela/ui/home/Fecha.kt`:
```kotlin
package com.fabri.abuela.ui.home

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ES = Locale("es", "ES")
private val FORMATO = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", ES)

fun fechaLarga(d: LocalDate): String = d.format(FORMATO).replaceFirstChar { it.titlecase(ES) }
```
  Run el mismo test → Expected: PASS.

- [ ] **Step 3: Pantallas y controlador**
  `…/abuela/navegacion/Pantalla.kt`:
```kotlin
package com.fabri.abuela.navegacion

sealed interface Pantalla {
    data object Inicio : Pantalla
    data object Familia : Pantalla
    data object TarjetaDia : Pantalla
    data object Error : Pantalla
}
```
  `…/abuela/navegacion/Controlador.kt`:
```kotlin
package com.fabri.abuela.navegacion

import com.fabri.abuela.acciones.Accion
import com.fabri.abuela.acciones.Lanzador
import com.fabri.abuela.acciones.Resultado
import com.fabri.abuela.feedback.Antirrebote
import com.fabri.abuela.feedback.Voz
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
}
```

- [ ] **Step 4: Cabecera, inicio y error**
  `…/abuela/ui/home/Cabecera.kt`:
```kotlin
package com.fabri.abuela.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun Cabecera(temperatura: String? = null, onToqueReloj: () -> Unit = {}) {
    var ahora by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { ahora = LocalDateTime.now(); delay(1_000) } }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                ahora.format(DateTimeFormatter.ofPattern("HH:mm")),
                fontSize = 64.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) { onToqueReloj() }
            )
            Spacer(Modifier.weight(1f))
            if (temperatura != null) Text(temperatura, fontSize = 40.sp)
        }
        Text(fechaLarga(ahora.toLocalDate()), fontSize = 26.sp)
    }
}
```
  `…/abuela/ui/home/HomeScreen.kt`:
```kotlin
package com.fabri.abuela.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fabri.abuela.acciones.Accion
import com.fabri.abuela.navegacion.Controlador
import com.fabri.abuela.ui.components.BotonGrande
import com.fabri.abuela.ui.theme.Colores
import com.fabri.abuela.ui.theme.Medidas

@Composable
fun HomeScreen(
    controlador: Controlador,
    temperatura: String?,
    onToqueReloj: () -> Unit,
    onFamilia: () -> Unit,
    onDia: () -> Unit
) {
    val sep = Arrangement.spacedBy(Medidas.separacion)
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion), verticalArrangement = sep) {
        Cabecera(temperatura, onToqueReloj)
        Row(Modifier.weight(1f), horizontalArrangement = sep) {
            BotonGrande("👨‍👩‍👧", "FAMILIA", "Llamar y mensajes", Colores.familia, Modifier.weight(1f).fillMaxHeight()) {
                if (controlador.permitir("Familia")) onFamilia()
            }
            BotonGrande("📷", "CÁMARA", "Hacer fotos", Colores.camara, Modifier.weight(1f).fillMaxHeight()) {
                controlador.pulsar(Accion.Camara, "Abriendo la cámara")
            }
        }
        Row(Modifier.weight(1f), horizontalArrangement = sep) {
            BotonGrande("▶️", "YOUTUBE", "Ver vídeos", Colores.youtube, Modifier.weight(1f).fillMaxHeight()) {
                controlador.pulsar(Accion.YouTube, "Abriendo YouTube")
            }
            BotonGrande("💬", "PREGUNTAR", "Hablar con ChatGPT", Colores.chatgpt, Modifier.weight(1f).fillMaxHeight()) {
                controlador.pulsar(Accion.ChatGptVoz, "Abriendo ChatGPT")
            }
        }
        BotonGrande("🛒", "MI TARJETA DIA", "Enseñar en la caja", Colores.dia, Modifier.weight(0.8f).fillMaxWidth()) {
            if (controlador.permitir("Tarjeta del Día")) onDia()
        }
    }
}
```
  `…/abuela/ui/error/ErrorScreen.kt`:
```kotlin
package com.fabri.abuela.ui.error

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.fabri.abuela.ui.components.BotonVolver
import com.fabri.abuela.ui.theme.Medidas

@Composable
fun ErrorScreen(nombreAyuda: String, onVolver: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Text("😕", fontSize = 96.sp)
        Text("No se ha podido abrir.", fontSize = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("Pídele ayuda a $nombreAyuda.", fontSize = 32.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        BotonVolver(onVolver)
    }
}
```

- [ ] **Step 5: Navegación**
  `…/abuela/navegacion/AppAbuela.kt` (Familia y TarjetaDia muestran de momento un texto provisional; las Tareas 10 y 14 los sustituyen):
```kotlin
package com.fabri.abuela.navegacion

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.fabri.abuela.ui.components.BotonVolver
import com.fabri.abuela.ui.error.ErrorScreen
import com.fabri.abuela.ui.home.HomeScreen

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
            onFamilia = { pantalla = Pantalla.Familia }, onDia = { pantalla = Pantalla.TarjetaDia }
        )
        Pantalla.Error -> ErrorScreen("Fabri", volver)
        Pantalla.Familia, Pantalla.TarjetaDia -> Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Text("En construcción", fontSize = 32.sp, modifier = Modifier.weight(1f))
            BotonVolver(volver)
        }
    }
}
```

- [ ] **Step 6: MainActivity definitiva (versión base)**
  Sustituye `…/abuela/MainActivity.kt` por:
```kotlin
package com.fabri.abuela

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableIntStateOf
import com.fabri.abuela.acciones.Ejecutor
import com.fabri.abuela.acciones.Lanzador
import com.fabri.abuela.feedback.Antirrebote
import com.fabri.abuela.feedback.Voz
import com.fabri.abuela.feedback.vibrarCorto
import com.fabri.abuela.navegacion.AppAbuela
import com.fabri.abuela.navegacion.Controlador
import com.fabri.abuela.ui.theme.TemaAbuela

class MainActivity : ComponentActivity() {
    private val irAInicio = mutableIntStateOf(0)
    private lateinit var voz: Voz
    private lateinit var controlador: Controlador

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voz = Voz(this)
        val ejecutor = Ejecutor(this)
        controlador = Controlador(Lanzador(ejecutor::abrir), Antirrebote(), voz) { vibrarCorto(this) }
        setContent {
            TemaAbuela { AppAbuela(controlador, irAInicio.intValue) }
        }
    }

    /** Pulsar ○ estando ya en el launcher → volver a la pantalla de inicio. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        irAInicio.intValue++
    }

    override fun onDestroy() { voz.cerrar(); super.onDestroy() }
}
```
  Borra `…/abuela/Sondas.kt`.

- [ ] **Step 7: Probar en el Redmi**
  Run ▶ y comprueba:
  - [ ] Se ven el reloj, la fecha y 5 botones sin scroll.
  - [ ] CÁMARA abre la cámara.
  - [ ] YOUTUBE abre YouTube.
  - [ ] PREGUNTAR abre ChatGPT en modo voz, o ChatGPT normal si no había atajo.
  - [ ] Al tocar, el móvil vibra y una voz dice "Abriendo…".
  - [ ] Dos toques rápidos en CÁMARA solo la abren una vez.
  - [ ] FAMILIA muestra "En construcción", y ◀ VOLVER o ◁ vuelven al inicio.
  - [ ] Estando en "En construcción", ○ vuelve al inicio.
  - [ ] Desinstala YouTube temporalmente, o cambia `Paquetes.YOUTUBE` por un nombre falso: debe salir la pantalla 😕. Después déjalo como estaba.

- [ ] **Step 8: Tests y commit**
  Run: `.\gradlew.bat testDebugUnitTest` → Expected: PASS.
```powershell
git add .
git commit -m "feat: pantalla de inicio, navegación y pantalla de error"
```

---

## FASE 2 — Familia y mensajes

### Task 9: Datos persistentes (familiares, ajustes, caché del tiempo)

**Files:**
- Create: `…/abuela/datos/Modelos.kt`, `…/abuela/datos/Codec.kt`, `…/abuela/datos/Repositorio.kt`
- Test: `test/…/abuela/datos/CodecTest.kt`

**Interfaces:**
- Produces:
  - `@Serializable data class Familiar(id: String, nombre: String, telefono: String, rutaFoto: String? = null, orden: Int = 0)`
  - `fun inicial(nombre: String): String`
  - `enum class TipoTarjeta { QR, BARRAS, APP }`
  - `@Serializable data class Ajustes(ciudad: String = "", lat: Double? = null, lon: Double? = null, tarjetaNumero: String = "", tarjetaTipo: TipoTarjeta = TipoTarjeta.QR, vozActivada: Boolean = true, nombreAyuda: String = "Fabri", pinHash: String = "", pinSal: String = "", configurado: Boolean = false)`
  - `@Serializable data class TiempoCache(tempC: Double, codigo: Int, horaMs: Long)`
  - `object Codec { fun familiares(s: String?): List<Familiar>; fun texto(l: List<Familiar>): String; fun ajustes(s: String?): Ajustes; fun texto(a: Ajustes): String; fun tiempo(s: String?): TiempoCache?; fun texto(t: TiempoCache): String }`
  - `class Repositorio(ctx: Context) { val familiares: Flow<List<Familiar>>; val ajustes: Flow<Ajustes>; val tiempo: Flow<TiempoCache?>; suspend fun guardarFamiliares(l); suspend fun guardarAjustes(a); suspend fun guardarTiempo(t); suspend fun copiarFoto(uri: Uri, id: String): String? }`

- [ ] **Step 1: Tests (Review Focus 2)**
  `test/…/abuela/datos/CodecTest.kt`:
```kotlin
package com.fabri.abuela.datos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CodecTest {
    @Test fun familiaresIdaYVuelta() {
        val l = listOf(Familiar("1", "Fabri", "+34612345678", null, 0), Familiar("2", "María", "600111222", "/x.jpg", 1))
        assertEquals(l, Codec.familiares(Codec.texto(l)))
    }
    @Test fun familiaresCorruptosDanListaVacia() {
        assertEquals(emptyList<Familiar>(), Codec.familiares("{basura"))
        assertEquals(emptyList<Familiar>(), Codec.familiares(null))
    }
    @Test fun ajustesCorruptosDanValoresPorDefecto() = assertEquals(Ajustes(), Codec.ajustes("no-json"))
    @Test fun ajustesConCampoDesconocidoSeLeen() =
        assertEquals("Fabri", Codec.ajustes("""{"nombreAyuda":"Fabri","campoNuevo":1}""").nombreAyuda)
    @Test fun tiempoVacioEsNull() = assertNull(Codec.tiempo(""))

    @Test fun inicialNormal() = assertEquals("F", inicial("fabri"))
    @Test fun inicialConEspacios() = assertEquals("M", inicial("  maría"))
    @Test fun inicialVacia() = assertEquals("?", inicial(""))
    @Test fun inicialSoloEspacios() = assertEquals("?", inicial("   "))
}
```
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.datos.CodecTest"` → Expected: FAIL (no compila).

- [ ] **Step 2: Modelos y Codec**
  `…/abuela/datos/Modelos.kt`:
```kotlin
package com.fabri.abuela.datos

import kotlinx.serialization.Serializable

@Serializable
data class Familiar(
    val id: String,
    val nombre: String,
    val telefono: String,
    val rutaFoto: String? = null,
    val orden: Int = 0
)

fun inicial(nombre: String): String =
    nombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

enum class TipoTarjeta { QR, BARRAS, APP }

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
    val configurado: Boolean = false
)

@Serializable
data class TiempoCache(val tempC: Double, val codigo: Int, val horaMs: Long)
```
  `…/abuela/datos/Codec.kt`:
```kotlin
package com.fabri.abuela.datos

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object Codec {
    private val json = Json { ignoreUnknownKeys = true }

    fun familiares(s: String?): List<Familiar> =
        s?.let { runCatching { json.decodeFromString<List<Familiar>>(it) }.getOrNull() } ?: emptyList()
    fun texto(l: List<Familiar>): String = json.encodeToString(l)

    fun ajustes(s: String?): Ajustes =
        s?.let { runCatching { json.decodeFromString<Ajustes>(it) }.getOrNull() } ?: Ajustes()
    fun texto(a: Ajustes): String = json.encodeToString(a)

    fun tiempo(s: String?): TiempoCache? =
        s?.takeIf { it.isNotBlank() }?.let { runCatching { json.decodeFromString<TiempoCache>(it) }.getOrNull() }
    fun texto(t: TiempoCache): String = json.encodeToString(t)
}
```
  Run el test → Expected: PASS (9 tests).

- [ ] **Step 3: Repositorio**
  `…/abuela/datos/Repositorio.kt`:
```kotlin
package com.fabri.abuela.datos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private val Context.almacen by preferencesDataStore("abuela")

class Repositorio(private val ctx: Context) {
    private val kFamiliares = stringPreferencesKey("familiares")
    private val kAjustes = stringPreferencesKey("ajustes")
    private val kTiempo = stringPreferencesKey("tiempo")

    private val prefs = ctx.almacen.data.catch { e ->
        Log.e("Repositorio", "Error leyendo DataStore", e); emit(emptyPreferences())
    }

    val familiares: Flow<List<Familiar>> = prefs.map { p -> Codec.familiares(p[kFamiliares]).sortedBy { it.orden } }
    val ajustes: Flow<Ajustes> = prefs.map { Codec.ajustes(it[kAjustes]) }
    val tiempo: Flow<TiempoCache?> = prefs.map { Codec.tiempo(it[kTiempo]) }

    suspend fun guardarFamiliares(l: List<Familiar>) {
        val ordenados = l.mapIndexed { i, f -> f.copy(orden = i) }
        ctx.almacen.edit { it[kFamiliares] = Codec.texto(ordenados) }
    }
    suspend fun guardarAjustes(a: Ajustes) { ctx.almacen.edit { it[kAjustes] = Codec.texto(a) } }
    suspend fun guardarTiempo(t: TiempoCache) { ctx.almacen.edit { it[kTiempo] = Codec.texto(t) } }

    /** Copia la foto elegida dentro de la app, reducida a 512 px como máximo. Devuelve la ruta, o null si falla. */
    suspend fun copiarFoto(uri: Uri, id: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val original = ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return@runCatching null
            val escala = 512f / maxOf(original.width, original.height)
            val bmp = if (escala < 1f) Bitmap.createScaledBitmap(
                original, (original.width * escala).toInt(), (original.height * escala).toInt(), true
            ) else original
            val dir = File(ctx.filesDir, "fotos").apply { mkdirs() }
            val f = File(dir, "$id.jpg")
            f.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            f.absolutePath
        }.getOrNull()
    }
}
```

- [ ] **Step 4: Compilar y commit**
  Run: `.\gradlew.bat testDebugUnitTest` → Expected: PASS.
```powershell
git add .
git commit -m "feat: modelos y repositorio con DataStore"
```

---

### Task 10: Pantalla Familia y confirmación de videollamada

**Files:**
- Create: `…/abuela/ui/components/FotoFamiliar.kt`, `…/abuela/ui/familia/TarjetaFamiliar.kt`, `…/abuela/ui/familia/FamiliaScreen.kt`, `…/abuela/ui/familia/ConfirmarScreen.kt`
- Modify: `…/abuela/navegacion/Pantalla.kt`, `…/abuela/navegacion/AppAbuela.kt`, `…/abuela/MainActivity.kt`

**Interfaces:**
- Consumes: `Familiar`, `inicial`, `Repositorio.familiares`, `Repositorio.ajustes`, `Controlador`, `Accion.WhatsAppVideo/WhatsAppChat/AbrirWhatsApp`
- Produces:
  - `@Composable fun FotoFamiliar(nombre: String, rutaFoto: String?, tamano: Dp)`
  - `@Composable fun FamiliaScreen(familiares: List<Familiar>, nuevosDe: (Familiar) -> Int, cabecera: @Composable () -> Unit, onVideo: (Familiar) -> Unit, onMensajes: (Familiar) -> Unit, onAbrirWhatsApp: () -> Unit, onVolver: () -> Unit)`
  - `@Composable fun ConfirmarScreen(familiar: Familiar, onSi: () -> Unit, onNo: () -> Unit)`
  - `Pantalla.Confirmar(familiar: Familiar)`
  - `AppAbuela(controlador, irAInicio, repo: Repositorio)`, que ahora recibe el repositorio.

- [ ] **Step 1: Foto con plan B a la inicial**
  `…/abuela/ui/components/FotoFamiliar.kt`:
```kotlin
package com.fabri.abuela.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.fabri.abuela.datos.inicial

@Composable
fun FotoFamiliar(nombre: String, rutaFoto: String?, tamano: Dp) {
    val bmp = remember(rutaFoto) {
        rutaFoto?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }
    }
    val m = Modifier.size(tamano).clip(CircleShape)
    if (bmp != null) {
        Image(bmp.asImageBitmap(), contentDescription = nombre, modifier = m, contentScale = ContentScale.Crop)
    } else {
        Box(m.background(Color(0xFF90CAF9)), contentAlignment = Alignment.Center) {
            Text(inicial(nombre), fontSize = (tamano.value / 2).sp, fontWeight = FontWeight.Bold)
        }
    }
}
```

- [ ] **Step 2: Tarjeta de familiar**
  `…/abuela/ui/familia/TarjetaFamiliar.kt`:
```kotlin
package com.fabri.abuela.ui.familia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.abuela.datos.Familiar
import com.fabri.abuela.ui.components.BotonGrande
import com.fabri.abuela.ui.components.FotoFamiliar
import com.fabri.abuela.ui.theme.Colores
import com.fabri.abuela.ui.theme.Medidas

@Composable
fun TarjetaFamiliar(f: Familiar, nuevos: Int, onVideo: () -> Unit, onMensajes: () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = Colores.familia.copy(alpha = 0.35f), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Medidas.separacion), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FotoFamiliar(f.nombre, f.rutaFoto, 96.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(f.nombre, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    if (nuevos > 0) Text(
                        "🔴 $nuevos ${if (nuevos == 1) "nuevo" else "nuevos"}",
                        fontSize = 24.sp, color = Colores.aviso, fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonGrande("📹", "VIDEOLLAMADA", null, Colores.si, Modifier.weight(1f), contentColor = Color.White, onClick = onVideo)
                BotonGrande("🎤", "MENSAJES", null, Color(0xFF1565C0), Modifier.weight(1f), contentColor = Color.White, onClick = onMensajes)
            }
        }
    }
}
```

- [ ] **Step 3: Pantalla Familia y confirmación**
  `…/abuela/ui/familia/FamiliaScreen.kt`:
```kotlin
package com.fabri.abuela.ui.familia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fabri.abuela.datos.Familiar
import com.fabri.abuela.ui.components.BotonGrande
import com.fabri.abuela.ui.components.BotonVolver
import com.fabri.abuela.ui.theme.Colores
import com.fabri.abuela.ui.theme.Medidas

@Composable
fun FamiliaScreen(
    familiares: List<Familiar>,
    nuevosDe: (Familiar) -> Int,
    cabecera: @Composable () -> Unit,
    onVideo: (Familiar) -> Unit,
    onMensajes: (Familiar) -> Unit,
    onAbrirWhatsApp: () -> Unit,
    onVolver: () -> Unit
) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion), verticalArrangement = Arrangement.spacedBy(Medidas.separacion)) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Medidas.separacion)) {
            item { cabecera() }
            items(familiares, key = { it.id }) { f ->
                TarjetaFamiliar(f, nuevosDe(f), onVideo = { onVideo(f) }, onMensajes = { onMensajes(f) })
            }
            item {
                BotonGrande("💬", "ABRIR WHATSAPP", "Ver todos los chats", Colores.whatsapp, Modifier.fillMaxWidth()) { onAbrirWhatsApp() }
            }
        }
        BotonVolver(onVolver)
    }
}
```
  `…/abuela/ui/familia/ConfirmarScreen.kt`:
```kotlin
package com.fabri.abuela.ui.familia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.abuela.datos.Familiar
import com.fabri.abuela.ui.components.BotonGrande
import com.fabri.abuela.ui.components.FotoFamiliar
import com.fabri.abuela.ui.theme.Colores
import com.fabri.abuela.ui.theme.Medidas

@Composable
fun ConfirmarScreen(familiar: Familiar, onSi: () -> Unit, onNo: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        FotoFamiliar(familiar.nombre, familiar.rutaFoto, 160.dp)
        Text("¿Llamar a ${familiar.nombre}?", fontSize = 40.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(Medidas.separacion)) {
            BotonGrande("✅", "SÍ, LLAMAR", null, Colores.si, Modifier.weight(1f).height(160.dp), contentColor = Color.White, onClick = onSi)
            BotonGrande("✖️", "NO", null, Colores.no, Modifier.weight(1f).height(160.dp), contentColor = Color.White, onClick = onNo)
        }
    }
}
```

- [ ] **Step 4: Conectar en la navegación**
  En `Pantalla.kt` añade dentro de `sealed interface Pantalla`:
```kotlin
    data class Confirmar(val familiar: com.fabri.abuela.datos.Familiar) : Pantalla
```
  Sustituye `AppAbuela.kt` por:
```kotlin
package com.fabri.abuela.navegacion

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.fabri.abuela.acciones.Accion
import com.fabri.abuela.datos.Ajustes
import com.fabri.abuela.datos.Repositorio
import com.fabri.abuela.ui.components.BotonVolver
import com.fabri.abuela.ui.error.ErrorScreen
import com.fabri.abuela.ui.familia.ConfirmarScreen
import com.fabri.abuela.ui.familia.FamiliaScreen
import com.fabri.abuela.ui.home.HomeScreen

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
            onFamilia = { pantalla = Pantalla.Familia }, onDia = { pantalla = Pantalla.TarjetaDia }
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
        is Pantalla.Confirmar -> ConfirmarScreen(
            familiar = p.familiar,
            onSi = {
                pantalla = Pantalla.Familia
                controlador.pulsar(Accion.WhatsAppVideo(p.familiar.telefono), "Llamando a ${p.familiar.nombre}")
            },
            onNo = { pantalla = Pantalla.Familia }
        )
        Pantalla.TarjetaDia -> Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Text("En construcción", fontSize = 32.sp, modifier = Modifier.weight(1f))
            BotonVolver(volver)
        }
    }
}
```
  > El "SÍ" usa el antirrebote dentro de `pulsar()`. Además, al tocarlo la pantalla cambia enseguida, así que un segundo toque ya no cae sobre el botón (Review Focus 5). Pero el toque que abrió la confirmación también pasó por el antirrebote, así que si "SÍ" se pulsa en menos de 1,5 s, ese toque se ignora y la abuela tendría que volver a llamar desde la lista. Por eso, en esta pantalla el antirrebote se reinicia al entrar: ver el Paso 5.

- [ ] **Step 5: Reiniciar el antirrebote al entrar en la confirmación**
  En `Antirrebote.kt` añade el método:
```kotlin
    @Synchronized fun reiniciar() { ultimo = null }
```
  y en `AntirreboteTest.kt` el test:
```kotlin
    @Test fun reiniciarPermiteElSiguienteToque() {
        assertTrue(a.intentar()); a.reiniciar(); ahora += 100
        assertTrue(a.intentar()); ahora += 100
        assertFalse(a.intentar())
    }
```
  En `Controlador.kt` añade:
```kotlin
    fun nuevaPantallaDeConfirmacion() = antirrebote.reiniciar()
```
  En `AppAbuela.kt`, justo antes de `ConfirmarScreen(`, dentro de `is Pantalla.Confirmar ->`, envuelve la llamada así:
```kotlin
        is Pantalla.Confirmar -> {
            LaunchedEffect(p) { controlador.nuevaPantallaDeConfirmacion() }
            ConfirmarScreen(
                /* …mismos parámetros que en el Paso 4… */
            )
        }
```
  (Copia dentro los mismos `familiar = …`, `onSi = {…}` y `onNo = {…}` del Paso 4.)
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.feedback.AntirreboteTest"` → Expected: PASS (6 tests).

- [ ] **Step 6: Pasar el repositorio desde MainActivity**
  En `MainActivity.kt`:
  - añade el campo `private lateinit var repo: com.fabri.abuela.datos.Repositorio`;
  - en `onCreate`, antes de `setContent`, añade `repo = com.fabri.abuela.datos.Repositorio(this)`;
  - cambia la llamada a `AppAbuela(controlador, irAInicio.intValue, repo)`.

- [ ] **Step 7: Familiar de prueba temporal**
  Como el modo administrador llega en la Tarea 17, de momento añade un familiar a mano. En `onCreate`, justo después de crear `repo`, añade:
```kotlin
        // TEMPORAL — se borra en la Tarea 17
        androidx.lifecycle.lifecycleScope.launch {
            repo.guardarFamiliares(listOf(com.fabri.abuela.datos.Familiar("1", "Fabri", "+34TU_NUMERO_REAL")))
        }
```
  Sustituye `+34TU_NUMERO_REAL` por tu número. Importa `kotlinx.coroutines.launch` y `androidx.lifecycle.lifecycleScope` con Alt+Enter. Pon un comentario `// TEMPORAL` encima para encontrarlo luego.

- [ ] **Step 8: Probar en el Redmi**
  - [ ] FAMILIA muestra la tarjeta de "Fabri" con una "F" en un círculo.
  - [ ] MENSAJES abre tu chat de WhatsApp directamente.
  - [ ] VIDEOLLAMADA muestra "¿Llamar a Fabri?". NO vuelve a la lista. SÍ abre la videollamada directa, o tu chat si falló R1.
  - [ ] Dos toques muy rápidos en SÍ solo hacen una llamada.
  - [ ] ABRIR WHATSAPP abre la lista de chats.
  - [ ] ◁ en la confirmación vuelve a Familia, y ◁ en Familia vuelve al inicio.

- [ ] **Step 9: Commit**
```powershell
git add .
git commit -m "feat: pantalla Familia con videollamada confirmada y chat directo"
```

---

### Task 11: Bandeja de mensajes nuevos (lógica pura)

**Files:**
- Create: `…/abuela/mensajes/Modelos.kt`, `…/abuela/mensajes/BandejaMensajes.kt`, `…/abuela/mensajes/Asociacion.kt`
- Test: `test/…/abuela/mensajes/BandejaMensajesTest.kt`, `test/…/abuela/mensajes/AsociacionTest.kt`

**Interfaces:**
- Consumes: `Familiar`
- Produces:
  - `data class NotifCruda(clave: String, esResumen: Boolean, categoria: String?, conversacion: String?, esGrupo: Boolean, mensajes: List<Pair<String, String>>, titulo: String?, texto: String?, hora: Long)`
  - `enum class TipoMensaje { TEXTO, VOZ, FOTO, VIDEO }`
  - `data class MensajeNuevo(clave: String, conversacion: String, remitente: String, texto: String, tipo: TipoMensaje, esGrupo: Boolean, hora: Long, cantidad: Int)` con `val resumen: String`
  - `class BandejaMensajes { val lista: StateFlow<List<MensajeNuevo>>; fun publicar(n: NotifCruda); fun quitar(clave: String); fun vaciar() }`
  - `object Asociacion { fun sinAcentos(s: String): String; fun familiarDe(m: MensajeNuevo, familiares: List<Familiar>, nombresAgenda: Map<String, String>): Familiar?; fun nuevosDe(f: Familiar, mensajes: List<MensajeNuevo>, familiares: List<Familiar>, nombresAgenda: Map<String, String>): Int }`
  - `nombresAgenda` es un mapa de `Familiar.id` al nombre que tiene ese teléfono en la agenda del móvil.

- [ ] **Step 1: Tests de la bandeja (Review Focus 3)**
  `test/…/abuela/mensajes/BandejaMensajesTest.kt`:
```kotlin
package com.fabri.abuela.mensajes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BandejaMensajesTest {
    private val b = BandejaMensajes()

    private fun individual(clave: String, quien: String, vararg textos: String, hora: Long = 1) = NotifCruda(
        clave = clave, esResumen = false, categoria = "msg", conversacion = null, esGrupo = false,
        mensajes = textos.map { quien to it }, titulo = quien, texto = textos.lastOrNull(), hora = hora
    )

    @Test fun chatIndividual() {
        b.publicar(individual("k1", "Fabri", "¿Has comido ya?"))
        val m = b.lista.value.single()
        assertEquals("Fabri", m.conversacion); assertEquals("Fabri", m.remitente)
        assertEquals("¿Has comido ya?", m.texto); assertEquals(TipoMensaje.TEXTO, m.tipo)
        assertEquals("¿Has comido ya?", m.resumen)
    }

    @Test fun mismaNotificacionActualizadaNoDuplica() {
        b.publicar(individual("k1", "Fabri", "Hola", hora = 1))
        b.publicar(individual("k1", "Fabri", "Hola", "¿Vienes?", hora = 2))
        val m = b.lista.value.single()
        assertEquals("¿Vienes?", m.texto); assertEquals(2, m.cantidad)
    }

    @Test fun resumenDeGrupoSeIgnora() {
        b.publicar(individual("r", "WhatsApp", "3 mensajes de 2 chats").copy(esResumen = true))
        assertTrue(b.lista.value.isEmpty())
    }

    @Test fun tituloWhatsAppSeIgnora() {
        b.publicar(individual("r2", "WhatsApp", "5 mensajes nuevos"))
        assertTrue(b.lista.value.isEmpty())
    }

    @Test fun llamadasYProgresoSeIgnoran() {
        b.publicar(individual("c", "Fabri", "Llamada entrante").copy(categoria = "call"))
        b.publicar(individual("p", "WhatsApp", "Copia de seguridad").copy(categoria = "progress"))
        assertTrue(b.lista.value.isEmpty())
    }

    @Test fun grupoConContadorEnTitulo() {
        b.publicar(NotifCruda("g", false, "msg", "Familia (3 mensajes)", true, listOf("María" to "Feliz día"), "Familia", null, 5))
        val m = b.lista.value.single()
        assertEquals("Familia", m.conversacion)
        assertEquals("Familia · María: Feliz día", m.resumen)
    }

    @Test fun tiposDeMensaje() {
        b.publicar(individual("a", "Ana", "🎤 Mensaje de voz (0:12)", hora = 1))
        b.publicar(individual("f", "Luis", "📷 Foto", hora = 2))
        b.publicar(individual("v", "Eva", "🎥 Vídeo", hora = 3))
        val tipos = b.lista.value.associate { it.remitente to it.tipo }
        assertEquals(TipoMensaje.VOZ, tipos["Ana"]); assertEquals(TipoMensaje.FOTO, tipos["Luis"]); assertEquals(TipoMensaje.VIDEO, tipos["Eva"])
        assertEquals("🎤 Mensaje de voz", b.lista.value.first { it.remitente == "Ana" }.resumen)
    }

    @Test fun quitarLaElimina() {
        b.publicar(individual("k1", "Fabri", "Hola")); b.quitar("k1")
        assertTrue(b.lista.value.isEmpty())
    }

    @Test fun masRecientePrimero() {
        b.publicar(individual("a", "Ana", "1", hora = 1)); b.publicar(individual("b", "Luis", "2", hora = 9))
        assertEquals("Luis", b.lista.value.first().remitente)
    }

    @Test fun sinMensajesUsaTituloYTexto() {
        b.publicar(NotifCruda("x", false, null, null, false, emptyList(), "Fabri", "Hola abuela", 1))
        assertEquals("Hola abuela", b.lista.value.single().texto)
    }

    @Test fun vaciaSeIgnora() {
        b.publicar(NotifCruda("x", false, null, null, false, emptyList(), null, null, 1))
        assertTrue(b.lista.value.isEmpty())
    }
}
```
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.mensajes.BandejaMensajesTest"` → Expected: FAIL (no compila).

- [ ] **Step 2: Implementar modelos y bandeja**
  `…/abuela/mensajes/Modelos.kt`:
```kotlin
package com.fabri.abuela.mensajes

data class NotifCruda(
    val clave: String,
    val esResumen: Boolean,
    val categoria: String?,
    val conversacion: String?,
    val esGrupo: Boolean,
    val mensajes: List<Pair<String, String>>,
    val titulo: String?,
    val texto: String?,
    val hora: Long
)

enum class TipoMensaje { TEXTO, VOZ, FOTO, VIDEO }

data class MensajeNuevo(
    val clave: String,
    val conversacion: String,
    val remitente: String,
    val texto: String,
    val tipo: TipoMensaje,
    val esGrupo: Boolean,
    val hora: Long,
    val cantidad: Int
) {
    private val cuerpo: String get() = when (tipo) {
        TipoMensaje.VOZ -> "🎤 Mensaje de voz"
        TipoMensaje.FOTO -> "📷 Foto"
        TipoMensaje.VIDEO -> "🎬 Vídeo"
        TipoMensaje.TEXTO -> texto
    }
    val resumen: String get() = if (esGrupo) "$conversacion · $remitente: $cuerpo" else cuerpo
}
```
  `…/abuela/mensajes/BandejaMensajes.kt`:
```kotlin
package com.fabri.abuela.mensajes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BandejaMensajes {
    private val _lista = MutableStateFlow<List<MensajeNuevo>>(emptyList())
    val lista: StateFlow<List<MensajeNuevo>> = _lista.asStateFlow()

    private val categoriasIgnoradas = setOf("call", "progress", "sys", "err", "service", "missed_call")
    private val contador = Regex("""\s*\(\d+\s+mensajes?\)\s*$""", RegexOption.IGNORE_CASE)

    fun publicar(n: NotifCruda) {
        if (n.esResumen || n.categoria in categoriasIgnoradas || n.titulo.equals("WhatsApp", ignoreCase = true)) return
        val ultimo = n.mensajes.lastOrNull()
        val remitente = ultimo?.first?.takeIf { it.isNotBlank() } ?: n.titulo.orEmpty()
        val texto = (ultimo?.second ?: n.texto).orEmpty().trim()
        if (remitente.isBlank() && texto.isBlank()) return
        val conversacion = (n.conversacion ?: n.titulo ?: remitente).replace(contador, "").trim()
        val nuevo = MensajeNuevo(
            clave = n.clave, conversacion = conversacion, remitente = remitente, texto = texto,
            tipo = tipoDe(texto), esGrupo = n.esGrupo, hora = n.hora, cantidad = maxOf(1, n.mensajes.size)
        )
        _lista.update { l -> (l.filterNot { it.clave == n.clave } + nuevo).sortedByDescending { it.hora } }
    }

    fun quitar(clave: String) = _lista.update { l -> l.filterNot { it.clave == clave } }
    fun vaciar() { _lista.value = emptyList() }

    private fun tipoDe(t: String): TipoMensaje {
        val s = t.lowercase()
        return when {
            s.startsWith("🎤") || "mensaje de voz" in s || s.startsWith("audio") -> TipoMensaje.VOZ
            s.startsWith("📷") || s == "foto" || s.startsWith("foto ") -> TipoMensaje.FOTO
            s.startsWith("🎥") || s.startsWith("🎬") || s.startsWith("vídeo") || s.startsWith("video") -> TipoMensaje.VIDEO
            else -> TipoMensaje.TEXTO
        }
    }
}
```
  Run el test → Expected: PASS (11 tests).

- [ ] **Step 3: Tests de asociación con familiares**
  `test/…/abuela/mensajes/AsociacionTest.kt`:
```kotlin
package com.fabri.abuela.mensajes

import com.fabri.abuela.datos.Familiar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AsociacionTest {
    private val fabri = Familiar("1", "Fabri", "+34612345678")
    private val maria = Familiar("2", "María", "600111222")
    private val familia = listOf(fabri, maria)
    private val agenda = mapOf("1" to "Fabrizio Nieto", "2" to "Maria Hija")

    private fun m(remitente: String, grupo: Boolean = false, cantidad: Int = 1) =
        MensajeNuevo("k$remitente", remitente, remitente, "hola", TipoMensaje.TEXTO, grupo, 1, cantidad)

    @Test fun porNombreDeAgenda() = assertEquals(fabri, Asociacion.familiarDe(m("Fabrizio Nieto"), familia, agenda))
    @Test fun sinAcentosNiMayusculas() = assertEquals(maria, Asociacion.familiarDe(m("MARÍA HIJA"), familia, agenda))
    @Test fun porNombreDelLauncherSiNoHayAgenda() = assertEquals(maria, Asociacion.familiarDe(m("maria"), familia, emptyMap()))
    @Test fun desconocidoEsNull() = assertNull(Asociacion.familiarDe(m("Banco"), familia, agenda))
    @Test fun grupoNoSeAsocia() = assertNull(Asociacion.familiarDe(m("Fabrizio Nieto", grupo = true), familia, agenda))
    @Test fun cuentaNuevos() =
        assertEquals(3, Asociacion.nuevosDe(fabri, listOf(m("Fabrizio Nieto", cantidad = 3), m("Banco")), familia, agenda))
}
```
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.mensajes.AsociacionTest"` → Expected: FAIL.

- [ ] **Step 4: Implementar la asociación**
  `…/abuela/mensajes/Asociacion.kt`:
```kotlin
package com.fabri.abuela.mensajes

import com.fabri.abuela.datos.Familiar
import java.text.Normalizer

object Asociacion {
    private val marcas = Regex("\\p{Mn}+")

    fun sinAcentos(s: String): String =
        Normalizer.normalize(s.trim().lowercase(), Normalizer.Form.NFD).replace(marcas, "")

    fun familiarDe(m: MensajeNuevo, familiares: List<Familiar>, nombresAgenda: Map<String, String>): Familiar? {
        if (m.esGrupo) return null
        val quien = sinAcentos(m.remitente)
        if (quien.isEmpty()) return null
        return familiares.firstOrNull { f -> nombresAgenda[f.id]?.let { sinAcentos(it) == quien } == true }
            ?: familiares.firstOrNull { sinAcentos(it.nombre) == quien }
    }

    fun nuevosDe(f: Familiar, mensajes: List<MensajeNuevo>, familiares: List<Familiar>, nombresAgenda: Map<String, String>): Int =
        mensajes.filter { familiarDe(it, familiares, nombresAgenda)?.id == f.id }.sumOf { it.cantidad }
}
```
  Run: `.\gradlew.bat testDebugUnitTest` → Expected: PASS.

- [ ] **Step 5: Commit**
```powershell
git add .
git commit -m "feat: bandeja de mensajes nuevos y asociación con familiares"
```

---

### Task 12: Lector de notificaciones de WhatsApp (riesgo R5)

**Files:**
- Create: `…/abuela/mensajes/Mensajes.kt`, `…/abuela/mensajes/LectorNotificaciones.kt`
- Modify: `app/src/main/AndroidManifest.xml`, `…/abuela/MainActivity.kt`

**Interfaces:**
- Consumes: `BandejaMensajes`, `NotifCruda`, `Paquetes.WHATSAPP`
- Produces:
  - `object Mensajes { val bandeja: BandejaMensajes; var conectado: Boolean; fun tienePermiso(ctx): Boolean; fun reconectar(ctx); fun abrir(activity: Activity, clave: String): Boolean }`
  - `class LectorNotificaciones : NotificationListenerService`

- [ ] **Step 1: Estado compartido**
  `…/abuela/mensajes/Mensajes.kt`:
```kotlin
package com.fabri.abuela.mensajes

import android.app.Activity
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.ConcurrentHashMap

object Mensajes {
    val bandeja = BandejaMensajes()
    internal val accesos = ConcurrentHashMap<String, PendingIntent>()
    @Volatile var conectado = false

    fun tienePermiso(ctx: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)

    fun reconectar(ctx: Context) {
        if (tienePermiso(ctx) && !conectado) {
            NotificationListenerService.requestRebind(ComponentName(ctx, LectorNotificaciones::class.java))
        }
    }

    /** Abre el chat exacto de la notificación. false si ya no existe. */
    fun abrir(activity: Activity, clave: String): Boolean {
        val pi = accesos[clave] ?: return false
        return try {
            val opciones = if (Build.VERSION.SDK_INT >= 34) ActivityOptions.makeBasic()
                .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                .toBundle() else null
            pi.send(activity, 0, null, null, null, null, opciones)
            true
        } catch (e: Exception) { false }
    }
}
```

- [ ] **Step 2: El servicio**
  `…/abuela/mensajes/LectorNotificaciones.kt`:
```kotlin
package com.fabri.abuela.mensajes

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fabri.abuela.BuildConfig
import com.fabri.abuela.acciones.Paquetes

class LectorNotificaciones : NotificationListenerService() {

    override fun onListenerConnected() {
        Mensajes.conectado = true
        Mensajes.bandeja.vaciar(); Mensajes.accesos.clear()
        activeNotifications?.forEach { onNotificationPosted(it) }
    }

    override fun onListenerDisconnected() { Mensajes.conectado = false }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != Paquetes.WHATSAPP) return
        val cruda = runCatching { aCruda(sbn) }.getOrNull() ?: return
        if (BuildConfig.DEBUG) Log.i("SONDA_NOTIF", cruda.toString())
        Mensajes.bandeja.publicar(cruda)
        sbn.notification.contentIntent?.let { Mensajes.accesos[sbn.key] = it }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName != Paquetes.WHATSAPP) return
        Mensajes.bandeja.quitar(sbn.key); Mensajes.accesos.remove(sbn.key)
    }

    private fun aCruda(sbn: StatusBarNotification): NotifCruda {
        val n = sbn.notification
        val estilo = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(n)
        val extras = n.extras
        return NotifCruda(
            clave = sbn.key,
            esResumen = (n.flags and Notification.FLAG_GROUP_SUMMARY) != 0,
            categoria = n.category,
            conversacion = estilo?.conversationTitle?.toString(),
            esGrupo = estilo?.isGroupConversation ?: false,
            mensajes = estilo?.messages?.map { (it.person?.name?.toString().orEmpty()) to (it.text?.toString().orEmpty()) }.orEmpty(),
            titulo = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            texto = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            hora = sbn.postTime
        )
    }
}
```
  > Ojo: `onNotificationPosted` registra en Logcat el contenido de los mensajes, pero **solo en las versiones de depuración** (`BuildConfig.DEBUG`). Sirve para comprobar R5.

- [ ] **Step 3: Declarar el servicio y activar BuildConfig**
  En `AndroidManifest.xml`, dentro de `<application>` y después de `</activity>`:
```xml
        <service
            android:name=".mensajes.LectorNotificaciones"
            android:exported="false"
            android:label="Inicio Abuela - mensajes"
            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>
```
  En `app/build.gradle.kts`, dentro de `android { }`, añade o completa:
```kotlin
    buildFeatures { compose = true; buildConfig = true }
```
  (Si ya existe `buildFeatures { compose = true }`, añade `buildConfig = true` dentro.)

- [ ] **Step 4: Reconectar al volver al inicio**
  En `MainActivity.kt` añade:
```kotlin
    override fun onResume() {
        super.onResume()
        com.fabri.abuela.mensajes.Mensajes.reconectar(this)
    }
```

- [ ] **Step 5: Conceder el acceso y comprobar R5**
  1. Run ▶.
  2. En el Redmi: Ajustes → Notificaciones → (⋮ o "Más ajustes") **Acceso a notificaciones** → activa "Inicio Abuela - mensajes" y acepta. En HyperOS también se encuentra buscando "acceso a notificaciones" en el buscador de Ajustes.
  3. Desde otro móvil manda al Redmi un texto, un audio y una foto, y escribe en un grupo donde esté ella.
  4. En Logcat, con el filtro `tag:SONDA_NOTIF`, deben aparecer líneas `NotifCruda(...)` con remitente y texto. Comprueba también que la notificación de resumen trae `esResumen=true`.
  5. Apunta en `docs/fase0-resultados.md` → "R5: OK", o lo que haya salido distinto. Por ejemplo, si el texto del audio no empieza por 🎤, añade esa variante a `tipoDe()` en `BandejaMensajes` **con su test** en `BandejaMensajesTest.tiposDeMensaje`.

- [ ] **Step 6: Commit**
```powershell
git add .
git commit -m "feat: lector de notificaciones de WhatsApp"
```

---

### Task 13: Mensajes nuevos en la pantalla Familia

**Files:**
- Create: `…/abuela/ui/familia/SeccionMensajes.kt`
- Modify: `…/abuela/acciones/Accion.kt`, `…/abuela/acciones/Destino.kt`, `…/abuela/acciones/Lanzador.kt`, `…/abuela/acciones/Ejecutor.kt`, `…/abuela/navegacion/AppAbuela.kt`
- Test: `test/…/abuela/acciones/LanzadorTest.kt` (añadir tests)

**Interfaces:**
- Consumes: `Mensajes.bandeja.lista`, `Mensajes.abrir`, `Asociacion`, `WhatsAppContactos.nombreEnAgenda`, `FotoFamiliar`
- Produces:
  - `Accion.AbrirMensaje(clave: String, telefonoFamiliar: String?)`
  - `Destino.Notificacion(clave: String)`
  - `@Composable fun SeccionMensajes(mensajes: List<MensajeNuevo>, familiarDe: (MensajeNuevo) -> Familiar?, onAbrir: (MensajeNuevo) -> Unit)`

- [ ] **Step 1: Tests nuevos del Lanzador**
  Añade a `LanzadorTest.kt`:
```kotlin
    @Test fun abrirMensajeDeFamiliar() {
        val planes = Lanzador { false }.planes(Accion.AbrirMensaje("k1", "612345678"))
        assertEquals(
            listOf(Destino.Notificacion("k1"), Destino.Ver("https://wa.me/34612345678", Paquetes.WHATSAPP), Destino.AbrirApp(Paquetes.WHATSAPP)),
            planes
        )
    }

    @Test fun abrirMensajeDeDesconocido() {
        val planes = Lanzador { false }.planes(Accion.AbrirMensaje("k1", null))
        assertEquals(listOf(Destino.Notificacion("k1"), Destino.AbrirApp(Paquetes.WHATSAPP)), planes)
    }
```
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.acciones.LanzadorTest"` → Expected: FAIL (no compila).

- [ ] **Step 2: Implementar la acción**
  En `Accion.kt` añade:
```kotlin
    data class AbrirMensaje(val clave: String, val telefonoFamiliar: String?) : Accion
```
  En `Destino.kt` añade:
```kotlin
    data class Notificacion(val clave: String) : Destino
```
  En `Lanzador.planes`, añade la rama:
```kotlin
        is Accion.AbrirMensaje -> listOf(Destino.Notificacion(a.clave)) +
            chat(a.telefonoFamiliar.orEmpty()) + Destino.AbrirApp(Paquetes.WHATSAPP)
```
  En `Ejecutor.abrir`, añade la rama:
```kotlin
        is Destino.Notificacion -> com.fabri.abuela.mensajes.Mensajes.abrir(activity, d.clave)
```
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.acciones.LanzadorTest"` → Expected: PASS (10 tests).

- [ ] **Step 3: Sección visual**
  `…/abuela/ui/familia/SeccionMensajes.kt`:
```kotlin
package com.fabri.abuela.ui.familia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.abuela.datos.Familiar
import com.fabri.abuela.mensajes.MensajeNuevo
import com.fabri.abuela.ui.components.FotoFamiliar
import com.fabri.abuela.ui.theme.Colores
import com.fabri.abuela.ui.theme.Medidas

@Composable
fun SeccionMensajes(
    mensajes: List<MensajeNuevo>,
    familiarDe: (MensajeNuevo) -> Familiar?,
    onAbrir: (MensajeNuevo) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (mensajes.isEmpty()) {
            Text("No tienes mensajes nuevos", fontSize = Medidas.texto)
            return@Column
        }
        Text("📩 MENSAJES NUEVOS", fontSize = Medidas.titulo, fontWeight = FontWeight.Bold)
        mensajes.forEach { m ->
            val f = familiarDe(m)
            Surface(
                onClick = { onAbrir(m) }, shape = RoundedCornerShape(20.dp), color = Colores.mensaje,
                shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth().heightIn(min = Medidas.botonMin)
            ) {
                Row(Modifier.padding(Medidas.separacion), verticalAlignment = Alignment.CenterVertically) {
                    FotoFamiliar(f?.nombre ?: m.conversacion, f?.rutaFoto, 72.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(f?.nombre ?: m.conversacion, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(m.resumen, fontSize = Medidas.texto, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Conectar en AppAbuela**
  En `AppAbuela`, debajo de `val ajustes by …`, añade:
```kotlin
    val mensajes by com.fabri.abuela.mensajes.Mensajes.bandeja.lista.collectAsState()
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val nombresAgenda by produceState(emptyMap<String, String>(), familiares) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            familiares.mapNotNull { f ->
                com.fabri.abuela.acciones.WhatsAppContactos.nombreEnAgenda(ctx, f.telefono)?.let { f.id to it }
            }.toMap()
        }
    }
    val familiarDe = { m: com.fabri.abuela.mensajes.MensajeNuevo ->
        com.fabri.abuela.mensajes.Asociacion.familiarDe(m, familiares, nombresAgenda)
    }
```
  En la rama `Pantalla.Familia -> FamiliaScreen(…)` sustituye `nuevosDe` y `cabecera` por:
```kotlin
            nuevosDe = { f -> com.fabri.abuela.mensajes.Asociacion.nuevosDe(f, mensajes, familiares, nombresAgenda) },
            cabecera = {
                com.fabri.abuela.ui.familia.SeccionMensajes(mensajes, familiarDe) { m ->
                    val f = familiarDe(m)
                    controlador.pulsar(
                        Accion.AbrirMensaje(m.clave, f?.telefono),
                        "Abriendo los mensajes de ${f?.nombre ?: m.conversacion}"
                    )
                }
            },
```

- [ ] **Step 5: Probar en el Redmi**
  - [ ] Sin mensajes sin leer aparece "No tienes mensajes nuevos" en una sola línea.
  - [ ] Mándate un texto desde tu otro móvil: aparece la tarjeta con tu foto o inicial, y "🔴 1 nuevo" en tu tarjeta de familiar.
  - [ ] Al tocar la tarjeta se abre tu chat exacto. Tras leerlo y volver con ○, la tarjeta y el 🔴 han desaparecido.
  - [ ] Un audio sale como "🎤 Mensaje de voz".
  - [ ] Un mensaje de grupo sale como "Grupo · Nombre: texto", sin 🔴 en ningún familiar.
  - [ ] Un mensaje de alguien que no es familiar sale con su inicial, y al tocarlo se abre su chat.

- [ ] **Step 6: Commit**
```powershell
git add .
git commit -m "feat: mensajes nuevos y avisos en la pantalla Familia"
```

---

## FASE 3 — Tarjeta DIA y tiempo

### Task 14: Tarjeta DIA

**Files:**
- Create: `…/abuela/dia/CodigoTarjeta.kt`, `…/abuela/ui/dia/TarjetaDiaScreen.kt`
- Modify: `…/abuela/acciones/Paquetes.kt`, `…/abuela/acciones/Accion.kt`, `…/abuela/acciones/Lanzador.kt`, `app/src/main/AndroidManifest.xml`, `…/abuela/navegacion/AppAbuela.kt`
- Test: `test/…/abuela/dia/CodigoTarjetaTest.kt`, `test/…/abuela/acciones/LanzadorTest.kt` (añadir)

**Interfaces:**
- Consumes: `Ajustes.tarjetaNumero`, `Ajustes.tarjetaTipo`, `TipoTarjeta`
- Produces:
  - `object CodigoTarjeta { fun matriz(numero: String, tipo: TipoTarjeta, ancho: Int, alto: Int): BitMatrix? }`
  - `Paquetes.DIA`
  - `Accion.TarjetaDiaApp`
  - `@Composable fun TarjetaDiaScreen(numero: String, tipo: TipoTarjeta, onVolver: () -> Unit)`

- [ ] **Step 1: Tests del código**
  `test/…/abuela/dia/CodigoTarjetaTest.kt`:
```kotlin
package com.fabri.abuela.dia

import com.fabri.abuela.datos.TipoTarjeta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CodigoTarjetaTest {
    @Test fun qr() {
        val m = CodigoTarjeta.matriz("1234567890123", TipoTarjeta.QR, 600, 600)
        assertNotNull(m); assertEquals(600, m!!.width)
    }
    @Test fun barras() = assertNotNull(CodigoTarjeta.matriz("1234567890123", TipoTarjeta.BARRAS, 900, 300))
    @Test fun vacioEsNull() = assertNull(CodigoTarjeta.matriz("  ", TipoTarjeta.QR, 600, 600))
    @Test fun modoAppEsNull() = assertNull(CodigoTarjeta.matriz("123", TipoTarjeta.APP, 600, 600))
}
```
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.dia.CodigoTarjetaTest"` → Expected: FAIL.

- [ ] **Step 2: Implementar**
  `…/abuela/dia/CodigoTarjeta.kt`:
```kotlin
package com.fabri.abuela.dia

import com.fabri.abuela.datos.TipoTarjeta
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object CodigoTarjeta {
    fun matriz(numero: String, tipo: TipoTarjeta, ancho: Int, alto: Int): BitMatrix? {
        val n = numero.trim()
        if (n.isEmpty()) return null
        val formato = when (tipo) {
            TipoTarjeta.QR -> BarcodeFormat.QR_CODE
            TipoTarjeta.BARRAS -> BarcodeFormat.CODE_128
            TipoTarjeta.APP -> return null
        }
        return runCatching {
            MultiFormatWriter().encode(n, formato, ancho, alto, mapOf(EncodeHintType.MARGIN to 2))
        }.getOrNull()
    }
}
```
  Run → Expected: PASS (4 tests).
  > Si en la fase 0 la app DIA mostraba un código de barras EAN-13 y la caja no lee el CODE_128, cambia `BarcodeFormat.CODE_128` por `BarcodeFormat.EAN_13`. Para EAN-13 el número debe tener 12 o 13 dígitos.

- [ ] **Step 3: Modo app y paquete**
  En `Paquetes.kt` añade, con el paquete apuntado en la fase 0:
```kotlin
    const val DIA = "PEGA_AQUI_EL_PAQUETE_DE_FASE0"
```
  y sustituye ese texto por el paquete real; no debe quedar `PEGA_AQUI`.
  En el `<queries>` del Manifest añade `<package android:name="(el mismo paquete)" />`.
  En `Accion.kt` añade `data object TarjetaDiaApp : Accion`.
  En `Lanzador.planes` añade:
```kotlin
        Accion.TarjetaDiaApp -> listOf(Destino.Atajo(Paquetes.DIA, listOf("tarjeta", "card", "club")), Destino.AbrirApp(Paquetes.DIA))
```
  Test en `LanzadorTest.kt`:
```kotlin
    @Test fun tarjetaDiaSinAtajoAbreLaApp() {
        val l = lanzadorQueAcepta(Destino.AbrirApp(Paquetes.DIA))
        assertEquals(Resultado.PlanB, l.ejecutar(Accion.TarjetaDiaApp))
    }
```
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.acciones.LanzadorTest"` → Expected: PASS.

- [ ] **Step 4: Pantalla con brillo al máximo**
  `…/abuela/ui/dia/TarjetaDiaScreen.kt`:
```kotlin
package com.fabri.abuela.ui.dia

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color as AColor
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fabri.abuela.datos.TipoTarjeta
import com.fabri.abuela.dia.CodigoTarjeta
import com.fabri.abuela.ui.components.BotonVolver
import com.fabri.abuela.ui.theme.Medidas

@Composable
fun TarjetaDiaScreen(numero: String, tipo: TipoTarjeta, onVolver: () -> Unit) {
    val activity = LocalContext.current as Activity
    DisposableEffect(Unit) {
        val w = activity.window
        val antes = w.attributes.screenBrightness
        w.attributes = w.attributes.apply { screenBrightness = 1f }
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            w.attributes = w.attributes.apply { screenBrightness = antes }
            w.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    val (ancho, alto) = if (tipo == TipoTarjeta.BARRAS) 1000 to 350 else 800 to 800
    val bmp = remember(numero, tipo) {
        CodigoTarjeta.matriz(numero, tipo, ancho, alto)?.let { m ->
            Bitmap.createBitmap(m.width, m.height, Bitmap.Config.RGB_565).apply {
                for (x in 0 until m.width) for (y in 0 until m.height) setPixel(x, y, if (m[x, y]) AColor.BLACK else AColor.WHITE)
            }
        }
    }
    Column(
        Modifier.fillMaxSize().background(Color.White).safeDrawingPadding().padding(Medidas.separacion),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enseña esto en la caja", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (bmp != null) Image(bmp.asImageBitmap(), contentDescription = "Tarjeta DIA", modifier = Modifier.fillMaxWidth())
            else Text("Falta configurar la tarjeta", fontSize = 28.sp, color = Color.Black)
        }
        BotonVolver(onVolver)
    }
}
```

- [ ] **Step 5: Conectar en AppAbuela**
  En `HomeScreen(…)` dentro de `AppAbuela`, cambia `onDia` por:
```kotlin
            onDia = {
                when {
                    ajustes.tarjetaTipo == com.fabri.abuela.datos.TipoTarjeta.APP ->
                        controlador.pulsar(Accion.TarjetaDiaApp, "Abriendo la tarjeta del Día")
                    ajustes.tarjetaNumero.isBlank() -> controlador.mostrarError()
                    else -> pantalla = Pantalla.TarjetaDia
                }
            }
```
  Sustituye la rama `Pantalla.TarjetaDia -> Column { … }` por:
```kotlin
        Pantalla.TarjetaDia -> com.fabri.abuela.ui.dia.TarjetaDiaScreen(ajustes.tarjetaNumero, ajustes.tarjetaTipo, volver)
```
  Para probar sin el modo administrador, en el bloque `// TEMPORAL` de `MainActivity` añade también:
```kotlin
            repo.guardarAjustes(com.fabri.abuela.datos.Ajustes(tarjetaNumero = "TU_NUMERO_DIA", tarjetaTipo = com.fabri.abuela.datos.TipoTarjeta.QR))
```
  con tu número y el tipo de la fase 0.
  > Ojo: `HomeScreen` llama a `controlador.permitir()` **antes** de `onDia()`. Así, en modo `APP`, el `pulsar()` posterior se ignoraría por el antirrebote. Para evitarlo, en `HomeScreen.kt` cambia el botón DIA a `onDia()` sin `permitir()`. La navegación interna usa `permitir` en el propio `onDia` de AppAbuela:
```kotlin
        BotonGrande("🛒", "MI TARJETA DIA", "Enseñar en la caja", Colores.dia, Modifier.weight(0.8f).fillMaxWidth()) { onDia() }
```
  y en AppAbuela, la rama `else ->` queda así: `else -> if (controlador.permitir("Tarjeta del Día")) pantalla = Pantalla.TarjetaDia`.

- [ ] **Step 6: Probar en la tienda**
  - [ ] MI TARJETA DIA muestra el código, el brillo sube al máximo y al volver baja.
  - [ ] La pantalla no se apaga mientras el código está a la vista.
  - [ ] **En la caja de una tienda DIA real el lector lee el código.** Si no lo lee, prueba el otro formato (Paso 2) o el modo `APP`.

- [ ] **Step 7: Commit**
```powershell
git add .
git commit -m "feat: tarjeta DIA a pantalla completa con brillo máximo"
```

---

### Task 15: Temperatura con Open-Meteo

**Files:**
- Create: `…/abuela/tiempo/OpenMeteo.kt`, `…/abuela/tiempo/ServicioTiempo.kt`
- Modify: `…/abuela/navegacion/AppAbuela.kt`
- Test: `test/…/abuela/tiempo/OpenMeteoTest.kt`

**Interfaces:**
- Consumes: `TiempoCache`, `Repositorio.tiempo`, `Repositorio.guardarTiempo`, `Ajustes.lat/lon`
- Produces:
  - `@Serializable data class Ciudad(nombre: String, lat: Double, lon: Double, region: String?)`
  - `object OpenMeteo { fun urlActual(lat, lon): String; fun urlCiudad(nombre): String; fun parsearActual(texto: String?, ahoraMs: Long): TiempoCache?; fun parsearCiudades(texto: String?): List<Ciudad>; fun icono(codigo: Int): String; fun texto(c: TiempoCache): String; fun necesitaRefrescar(c: TiempoCache?, ahoraMs: Long): Boolean }`
  - `suspend fun descargar(url: String): String?`
  - `class ServicioTiempo(repo: Repositorio) { suspend fun refrescarSiToca(ajustes: Ajustes, cache: TiempoCache?) }`

- [ ] **Step 1: Tests (Review Focus 4)**
  `test/…/abuela/tiempo/OpenMeteoTest.kt`:
```kotlin
package com.fabri.abuela.tiempo

import com.fabri.abuela.datos.TiempoCache
import org.junit.Assert.*
import org.junit.Test

class OpenMeteoTest {
    @Test fun parseaActual() {
        val json = """{"latitude":40.4,"current":{"time":"2026-11-08T10:45","temperature_2m":14.3,"weather_code":3}}"""
        assertEquals(TiempoCache(14.3, 3, 99), OpenMeteo.parsearActual(json, 99))
    }
    @Test fun faltaTemperaturaEsNull() = assertNull(OpenMeteo.parsearActual("""{"current":{"weather_code":3}}""", 1))
    @Test fun faltaCurrentEsNull() = assertNull(OpenMeteo.parsearActual("""{"latitude":40.4}""", 1))
    @Test fun htmlDeErrorEsNull() = assertNull(OpenMeteo.parsearActual("<html>502 Bad Gateway</html>", 1))
    @Test fun nullEsNull() = assertNull(OpenMeteo.parsearActual(null, 1))
    @Test fun sinCodigoUsaDesconocido() =
        assertEquals(-1, OpenMeteo.parsearActual("""{"current":{"temperature_2m":10.0}}""", 1)!!.codigo)

    @Test fun ciudades() {
        val json = """{"results":[{"name":"Madrid","latitude":40.41,"longitude":-3.70,"admin1":"Madrid","country":"España"}]}"""
        assertEquals(listOf(Ciudad("Madrid", 40.41, -3.70, "Madrid")), OpenMeteo.parsearCiudades(json))
    }
    @Test fun sinResultadosListaVacia() = assertEquals(emptyList<Ciudad>(), OpenMeteo.parsearCiudades("""{"generationtime_ms":0.5}"""))
    @Test fun ciudadesBasura() = assertEquals(emptyList<Ciudad>(), OpenMeteo.parsearCiudades("xx"))

    @Test fun iconos() {
        assertEquals("☀️", OpenMeteo.icono(0)); assertEquals("⛅", OpenMeteo.icono(2)); assertEquals("☁️", OpenMeteo.icono(3))
        assertEquals("🌫️", OpenMeteo.icono(45)); assertEquals("🌧️", OpenMeteo.icono(61)); assertEquals("🌧️", OpenMeteo.icono(81))
        assertEquals("❄️", OpenMeteo.icono(73)); assertEquals("⛈️", OpenMeteo.icono(95)); assertEquals("🌡️", OpenMeteo.icono(-1))
    }
    @Test fun textoRedondea() = assertEquals("14°C ☁️", OpenMeteo.texto(TiempoCache(14.4, 3, 0)))
    @Test fun textoNegativo() = assertEquals("-2°C ❄️", OpenMeteo.texto(TiempoCache(-2.4, 71, 0)))

    @Test fun refrescoCada30Min() {
        assertTrue(OpenMeteo.necesitaRefrescar(null, 0))
        assertFalse(OpenMeteo.necesitaRefrescar(TiempoCache(1.0, 0, 0), 29 * 60_000L))
        assertTrue(OpenMeteo.necesitaRefrescar(TiempoCache(1.0, 0, 0), 30 * 60_000L))
        assertTrue(OpenMeteo.necesitaRefrescar(TiempoCache(1.0, 0, 10 * 60_000L), 0)) // reloj hacia atrás
    }
    @Test fun urlConPuntoDecimal() =
        assertTrue(OpenMeteo.urlActual(40.41, -3.7).contains("latitude=40.41&longitude=-3.7"))
}
```
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.tiempo.OpenMeteoTest"` → Expected: FAIL.

- [ ] **Step 2: Implementar OpenMeteo**
  `…/abuela/tiempo/OpenMeteo.kt`:
```kotlin
package com.fabri.abuela.tiempo

import com.fabri.abuela.datos.TiempoCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.roundToInt

@Serializable
data class Ciudad(val nombre: String, val lat: Double, val lon: Double, val region: String? = null)

@Serializable private data class Actual(
    @SerialName("temperature_2m") val temperatura: Double? = null,
    @SerialName("weather_code") val codigo: Int? = null
)
@Serializable private data class RespuestaActual(val current: Actual? = null)
@Serializable private data class CiudadApi(
    val name: String, val latitude: Double, val longitude: Double, val admin1: String? = null
)
@Serializable private data class RespuestaCiudades(val results: List<CiudadApi>? = null)

object OpenMeteo {
    private val json = Json { ignoreUnknownKeys = true }
    private const val TREINTA_MIN = 30 * 60_000L

    fun urlActual(lat: Double, lon: Double) =
        "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code&timezone=auto"

    fun urlCiudad(nombre: String) =
        "https://geocoding-api.open-meteo.com/v1/search?name=${URLEncoder.encode(nombre.trim(), "UTF-8")}&language=es&count=5"

    fun parsearActual(texto: String?, ahoraMs: Long): TiempoCache? {
        val r = texto?.let { runCatching { json.decodeFromString<RespuestaActual>(it) }.getOrNull() } ?: return null
        val t = r.current?.temperatura ?: return null
        return TiempoCache(t, r.current.codigo ?: -1, ahoraMs)
    }

    fun parsearCiudades(texto: String?): List<Ciudad> =
        texto?.let { runCatching { json.decodeFromString<RespuestaCiudades>(it) }.getOrNull() }
            ?.results.orEmpty().map { Ciudad(it.name, it.latitude, it.longitude, it.admin1) }

    fun icono(codigo: Int): String = when (codigo) {
        0 -> "☀️"
        1, 2 -> "⛅"
        3 -> "☁️"
        45, 48 -> "🌫️"
        in 51..67, in 80..82 -> "🌧️"
        in 71..77, 85, 86 -> "❄️"
        in 95..99 -> "⛈️"
        else -> "🌡️"
    }

    fun texto(c: TiempoCache) = "${c.tempC.roundToInt()}°C ${icono(c.codigo)}"

    fun necesitaRefrescar(c: TiempoCache?, ahoraMs: Long): Boolean =
        c == null || ahoraMs < c.horaMs || ahoraMs - c.horaMs >= TREINTA_MIN
}

suspend fun descargar(url: String): String? = withContext(Dispatchers.IO) {
    runCatching {
        val con = URL(url).openConnection() as HttpURLConnection
        con.connectTimeout = 8_000; con.readTimeout = 8_000
        try { if (con.responseCode == 200) con.inputStream.bufferedReader().readText() else null }
        finally { con.disconnect() }
    }.getOrNull()
}
```
  Run → Expected: PASS (15 tests).

- [ ] **Step 3: Servicio de refresco**
  `…/abuela/tiempo/ServicioTiempo.kt`:
```kotlin
package com.fabri.abuela.tiempo

import com.fabri.abuela.datos.Ajustes
import com.fabri.abuela.datos.Repositorio
import com.fabri.abuela.datos.TiempoCache

class ServicioTiempo(private val repo: Repositorio) {
    /** Sin ciudad o sin red: no hace nada y se mantiene la caché. */
    suspend fun refrescarSiToca(ajustes: Ajustes, cache: TiempoCache?) {
        val lat = ajustes.lat ?: return
        val lon = ajustes.lon ?: return
        val ahora = System.currentTimeMillis()
        if (!OpenMeteo.necesitaRefrescar(cache, ahora)) return
        OpenMeteo.parsearActual(descargar(OpenMeteo.urlActual(lat, lon)), ahora)?.let { repo.guardarTiempo(it) }
    }
}
```

- [ ] **Step 4: Mostrar en el inicio**
  En `AppAbuela`, debajo de `val ajustes by …`, añade:
```kotlin
    val tiempo by repo.tiempo.collectAsState(initial = null)
    val servicioTiempo = remember(repo) { com.fabri.abuela.tiempo.ServicioTiempo(repo) }
```
  Dentro de la rama `Pantalla.Inicio ->`, envuelve la llamada así:
```kotlin
        Pantalla.Inicio -> {
            LaunchedEffect(ajustes.lat, ajustes.lon) {
                while (true) {
                    servicioTiempo.refrescarSiToca(ajustes, tiempo)
                    kotlinx.coroutines.delay(60_000)
                }
            }
            HomeScreen(
                controlador = controlador,
                temperatura = tiempo?.let { com.fabri.abuela.tiempo.OpenMeteo.texto(it) },
                /* …resto de parámetros igual que antes… */
            )
        }
```
  (Copia dentro los mismos `onToqueReloj`, `onFamilia` y `onDia` que ya tenías.)
  > Mientras el bucle está esperando, `tiempo` se lee de una captura antigua. Para que lea siempre el valor actual, declara `val tiempoActual by rememberUpdatedState(tiempo)` justo antes del `LaunchedEffect` y usa `tiempoActual` dentro del bucle en vez de `tiempo`.
  Para probar, en el bloque `// TEMPORAL` de `Ajustes(...)` añade `ciudad = "Madrid", lat = 40.4168, lon = -3.7038`, o los datos de vuestra ciudad.

- [ ] **Step 5: Probar**
  - [ ] Al minuto como mucho aparece, por ejemplo, "14°C ☁️" junto al reloj.
  - [ ] En modo avión, cierra la app desde Ajustes → Apps → Forzar detención y pulsa ○: la temperatura sigue saliendo (es la caché).
  - [ ] Borra los datos de la app: sin ciudad no sale ninguna temperatura ni ningún error. Después vuelve a instalar.

- [ ] **Step 6: Commit**
```powershell
git add .
git commit -m "feat: temperatura actual con Open-Meteo y caché"
```

---

## FASE 4 — Modo administrador

### Task 16: PIN, bloqueo y contador de toques

**Files:**
- Create: `…/abuela/seguridad/Pin.kt`
- Test: `test/…/abuela/seguridad/PinTest.kt`

**Interfaces:**
- Produces:
  - `object Pin { fun valido(p: String): Boolean; fun nuevaSal(): String; fun hash(pin: String, sal: String): String; fun comprobar(pin: String, sal: String, hashGuardado: String): Boolean }`
  - `class IntentosPin(reloj: () -> Long, maxFallos: Int = 5, esperaMs: Long = 30_000) { fun bloqueado(): Boolean; fun segundosRestantes(): Int; fun fallo(); fun acierto() }`
  - `class ContadorToques(necesarios: Int = 5, ventanaMs: Long = 3_000, reloj: () -> Long) { fun toque(): Boolean }`

- [ ] **Step 1: Tests**
  `test/…/abuela/seguridad/PinTest.kt`:
```kotlin
package com.fabri.abuela.seguridad

import org.junit.Assert.*
import org.junit.Test

class PinTest {
    @Test fun formatos() {
        assertTrue(Pin.valido("1234")); assertTrue(Pin.valido("123456"))
        assertFalse(Pin.valido("123")); assertFalse(Pin.valido("1234567")); assertFalse(Pin.valido("12a4")); assertFalse(Pin.valido(""))
    }
    @Test fun hashYComprobacion() {
        val sal = Pin.nuevaSal()
        val h = Pin.hash("2580", sal)
        assertTrue(Pin.comprobar("2580", sal, h)); assertFalse(Pin.comprobar("0000", sal, h))
        assertNotEquals(h, Pin.hash("2580", Pin.nuevaSal()))
    }
    @Test fun sinPinGuardadoNuncaValida() = assertFalse(Pin.comprobar("", "", ""))

    @Test fun bloqueoTrasCincoFallos() {
        var t = 0L
        val i = IntentosPin({ t })
        repeat(4) { i.fallo() }; assertFalse(i.bloqueado())
        i.fallo(); assertTrue(i.bloqueado()); assertEquals(30, i.segundosRestantes())
        t += 30_000; assertFalse(i.bloqueado())
    }
    @Test fun aciertoReiniciaFallos() {
        val i = IntentosPin({ 0L }); repeat(4) { i.fallo() }; i.acierto(); i.fallo()
        assertFalse(i.bloqueado())
    }

    @Test fun cincoToquesEnTresSegundos() {
        var t = 0L
        val c = ContadorToques(reloj = { t })
        repeat(4) { assertFalse(c.toque()); t += 500 }
        assertTrue(c.toque())
    }
    @Test fun toquesLentosNoAbren() {
        var t = 0L
        val c = ContadorToques(reloj = { t })
        repeat(10) { assertFalse(c.toque()); t += 1_000 }
    }
}
```
  Run: `.\gradlew.bat testDebugUnitTest --tests "com.fabri.abuela.seguridad.PinTest"` → Expected: FAIL.

- [ ] **Step 2: Implementar**
  `…/abuela/seguridad/Pin.kt`:
```kotlin
package com.fabri.abuela.seguridad

import java.security.MessageDigest
import java.security.SecureRandom

object Pin {
    fun valido(p: String) = p.length in 4..6 && p.all(Char::isDigit)

    fun nuevaSal(): String {
        val b = ByteArray(16); SecureRandom().nextBytes(b)
        return b.joinToString("") { "%02x".format(it) }
    }

    fun hash(pin: String, sal: String): String =
        MessageDigest.getInstance("SHA-256").digest((sal + pin).toByteArray()).joinToString("") { "%02x".format(it) }

    fun comprobar(pin: String, sal: String, hashGuardado: String) =
        hashGuardado.isNotEmpty() && hash(pin, sal) == hashGuardado
}

class IntentosPin(
    private val reloj: () -> Long,
    private val maxFallos: Int = 5,
    private val esperaMs: Long = 30_000
) {
    private var fallos = 0
    private var bloqueadoHasta = 0L

    fun bloqueado() = reloj() < bloqueadoHasta
    fun segundosRestantes(): Int = (((bloqueadoHasta - reloj()).coerceAtLeast(0) + 999) / 1000).toInt()
    fun fallo() { fallos++; if (fallos >= maxFallos) { bloqueadoHasta = reloj() + esperaMs; fallos = 0 } }
    fun acierto() { fallos = 0 }
}

class ContadorToques(
    private val necesarios: Int = 5,
    private val ventanaMs: Long = 3_000,
    private val reloj: () -> Long
) {
    private val toques = ArrayDeque<Long>()

    fun toque(): Boolean {
        val t = reloj()
        toques.addLast(t)
        while (toques.isNotEmpty() && (t - toques.first() > ventanaMs || t < toques.first())) toques.removeFirst()
        if (toques.size >= necesarios) { toques.clear(); return true }
        return false
    }
}
```
  Run → Expected: PASS (7 tests).

- [ ] **Step 3: Commit**
```powershell
git add .
git commit -m "feat: PIN con hash y sal, bloqueo y contador de toques"
```

---

### Task 17: Pantallas de administración

**Files:**
- Create: `…/abuela/ui/admin/TecladoPin.kt`, `PinScreen.kt`, `AdminScreen.kt`, `AdminFamiliaresScreen.kt`, `EditarFamiliarScreen.kt`, `AjustesAdminScreen.kt`
- Modify: `…/abuela/navegacion/Pantalla.kt`, `…/abuela/navegacion/AppAbuela.kt`, `…/abuela/MainActivity.kt` (borrar el bloque `// TEMPORAL`)

**Interfaces:**
- Consumes: `Pin`, `IntentosPin`, `ContadorToques`, `Repositorio`, `Ajustes`, `Familiar`, `TipoTarjeta`, `OpenMeteo.urlCiudad/parsearCiudades`, `descargar`, `Ciudad`
- Produces:
  - `@Composable fun TecladoPin(titulo: String, mensaje: String?, onCompleto: (String) -> Unit, onCancelar: (() -> Unit)?)`
  - `@Composable fun PinScreen(ajustes: Ajustes, intentos: IntentosPin, onOk: () -> Unit, onCancelar: () -> Unit)`
  - `@Composable fun AdminScreen(onFamiliares, onAjustes, onDiagnostico, onSalirLauncher, onVolver)`
  - `@Composable fun AdminFamiliaresScreen(familiares, onEditar: (Familiar?) -> Unit, onGuardarLista: (List<Familiar>) -> Unit, onVolver)`
  - `@Composable fun EditarFamiliarScreen(familiar: Familiar?, onGuardar: (nombre: String, telefono: String, foto: Uri?) -> Unit, onCancelar)`
  - `@Composable fun AjustesAdminScreen(ajustes: Ajustes, onGuardar: (Ajustes) -> Unit, onVolver)`
  - `Pantalla.Pin`, `Pantalla.Admin`, `Pantalla.AdminFamiliares`, `Pantalla.EditarFamiliar(familiar: Familiar?)`, `Pantalla.AjustesAdmin`, `Pantalla.Diagnostico`

- [ ] **Step 1: Teclado PIN**
  `…/abuela/ui/admin/TecladoPin.kt`:
```kotlin
package com.fabri.abuela.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TecladoPin(titulo: String, mensaje: String?, onCompleto: (String) -> Unit, onCancelar: (() -> Unit)?) {
    var pin by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text(titulo, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("●".repeat(pin.length).ifEmpty { " " }, fontSize = 40.sp)
        if (mensaje != null) Text(mensaje, fontSize = 20.sp)
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("⌫", "0", "OK")).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { t ->
                    Button(onClick = {
                        when (t) {
                            "⌫" -> pin = pin.dropLast(1)
                            "OK" -> { onCompleto(pin); pin = "" }
                            else -> if (pin.length < 6) pin += t
                        }
                    }, modifier = Modifier.size(88.dp)) { Text(t, fontSize = 28.sp) }
                }
            }
        }
        if (onCancelar != null) TextButton(onClick = onCancelar) { Text("Cancelar", fontSize = 22.sp) }
    }
}
```

- [ ] **Step 2: Pantalla de PIN**
  `…/abuela/ui/admin/PinScreen.kt`:
```kotlin
package com.fabri.abuela.ui.admin

import androidx.compose.runtime.*
import com.fabri.abuela.datos.Ajustes
import com.fabri.abuela.seguridad.IntentosPin
import com.fabri.abuela.seguridad.Pin

@Composable
fun PinScreen(ajustes: Ajustes, intentos: IntentosPin, onOk: () -> Unit, onCancelar: () -> Unit) {
    var mensaje by remember { mutableStateOf<String?>(null) }
    TecladoPin("PIN de administrador", mensaje, onCompleto = { p ->
        when {
            intentos.bloqueado() -> mensaje = "Espera ${intentos.segundosRestantes()} s"
            Pin.comprobar(p, ajustes.pinSal, ajustes.pinHash) -> { intentos.acierto(); onOk() }
            else -> { intentos.fallo(); mensaje = if (intentos.bloqueado()) "Demasiados fallos. Espera 30 s" else "PIN incorrecto" }
        }
    }, onCancelar = onCancelar)
}
```

- [ ] **Step 3: Menú de administración**
  `…/abuela/ui/admin/AdminScreen.kt`:
```kotlin
package com.fabri.abuela.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fabri.abuela.ui.components.BotonGrande
import com.fabri.abuela.ui.components.BotonVolver
import com.fabri.abuela.ui.theme.Colores
import com.fabri.abuela.ui.theme.Medidas

@Composable
fun AdminScreen(onFamiliares: () -> Unit, onAjustes: () -> Unit, onDiagnostico: () -> Unit, onSalirLauncher: () -> Unit, onVolver: () -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(Medidas.separacion), verticalArrangement = Arrangement.spacedBy(Medidas.separacion)) {
        Text("Administración", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        BotonGrande("👨‍👩‍👧", "Familiares", null, Colores.familia, Modifier.fillMaxWidth(), onClick = onFamiliares)
        BotonGrande("⚙️", "Ajustes", "Tarjeta DIA, ciudad, voz, PIN", Colores.camara, Modifier.fillMaxWidth(), onClick = onAjustes)
        BotonGrande("🩺", "Comprobar botones", null, Colores.chatgpt, Modifier.fillMaxWidth(), onClick = onDiagnostico)
        BotonGrande("🏠", "Salir al launcher normal", "Elegir otra pantalla de inicio", Colores.volver, Modifier.fillMaxWidth(), onClick = onSalirLauncher)
        Spacer(Modifier.weight(1f))
        BotonVolver(onVolver)
    }
}
```

- [ ] **Step 4: Lista de familiares (añadir, editar, ordenar, borrar)**
  `…/abuela/ui/admin/AdminFamiliaresScreen.kt`:
```kotlin
package com.fabri.abuela.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.abuela.datos.Familiar
import com.fabri.abuela.ui.components.BotonVolver
import com.fabri.abuela.ui.components.FotoFamiliar

@Composable
fun AdminFamiliaresScreen(
    familiares: List<Familiar>,
    onEditar: (Familiar?) -> Unit,
    onGuardarLista: (List<Familiar>) -> Unit,
    onVolver: () -> Unit
) {
    var aBorrar by remember { mutableStateOf<Familiar?>(null) }
    fun mover(i: Int, d: Int) {
        val j = i + d
        if (j !in familiares.indices) return
        onGuardarLista(familiares.toMutableList().apply { add(j, removeAt(i)) })
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { onEditar(null) }, modifier = Modifier.fillMaxWidth().height(64.dp)) { Text("+ Añadir familiar", fontSize = 22.sp) }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(familiares, key = { _, f -> f.id }) { i, f ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FotoFamiliar(f.nombre, f.rutaFoto, 56.dp)
                    Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        Text(f.nombre, fontSize = 22.sp); Text(f.telefono, fontSize = 16.sp)
                    }
                    TextButton(onClick = { mover(i, -1) }) { Text("↑", fontSize = 24.sp) }
                    TextButton(onClick = { mover(i, 1) }) { Text("↓", fontSize = 24.sp) }
                    TextButton(onClick = { onEditar(f) }) { Text("✏️", fontSize = 24.sp) }
                    TextButton(onClick = { aBorrar = f }) { Text("🗑", fontSize = 24.sp) }
                }
            }
        }
        BotonVolver(onVolver)
    }
    aBorrar?.let { f ->
        AlertDialog(
            onDismissRequest = { aBorrar = null },
            title = { Text("¿Borrar a ${f.nombre}?") },
            confirmButton = { TextButton(onClick = { onGuardarLista(familiares - f); aBorrar = null }) { Text("Borrar") } },
            dismissButton = { TextButton(onClick = { aBorrar = null }) { Text("Cancelar") } }
        )
    }
}
```

- [ ] **Step 5: Editar un familiar**
  `…/abuela/ui/admin/EditarFamiliarScreen.kt`:
```kotlin
package com.fabri.abuela.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.abuela.acciones.Telefono
import com.fabri.abuela.datos.Familiar
import com.fabri.abuela.ui.components.FotoFamiliar

@Composable
fun EditarFamiliarScreen(familiar: Familiar?, onGuardar: (String, String, Uri?) -> Unit, onCancelar: () -> Unit) {
    var nombre by remember { mutableStateOf(familiar?.nombre.orEmpty()) }
    var telefono by remember { mutableStateOf(familiar?.telefono ?: "+34") }
    var foto by remember { mutableStateOf<Uri?>(null) }
    val elegir = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { foto = it ?: foto }
    val telOk = Telefono.normalizar(telefono).length >= 11
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(if (familiar == null) "Nuevo familiar" else "Editar familiar", fontSize = 28.sp)
        FotoFamiliar(nombre, familiar?.rutaFoto, 96.dp)
        if (foto != null) Text("Foto nueva elegida ✅", fontSize = 18.sp)
        Button(onClick = { elegir.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Elegir foto") }
        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre (como lo verá ella)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            telefono, { telefono = it }, label = { Text("Teléfono con prefijo (+34…)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(),
            isError = !telOk, supportingText = { if (!telOk) Text("Número incompleto") }
        )
        Text("Para la videollamada directa, este número debe estar guardado también en Contactos del móvil.", fontSize = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onGuardar(nombre.trim(), telefono.trim(), foto) }, enabled = nombre.isNotBlank() && telOk) { Text("Guardar") }
            OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
        }
    }
}
```

- [ ] **Step 6: Ajustes (ciudad, tarjeta, ayuda, voz, PIN)**
  `…/abuela/ui/admin/AjustesAdminScreen.kt`:
```kotlin
package com.fabri.abuela.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.abuela.datos.Ajustes
import com.fabri.abuela.datos.TipoTarjeta
import com.fabri.abuela.seguridad.Pin
import com.fabri.abuela.tiempo.Ciudad
import com.fabri.abuela.tiempo.OpenMeteo
import com.fabri.abuela.tiempo.descargar
import kotlinx.coroutines.launch

@Composable
fun AjustesAdminScreen(ajustes: Ajustes, onGuardar: (Ajustes) -> Unit, onVolver: () -> Unit) {
    var a by remember(ajustes) { mutableStateOf(ajustes) }
    var busqueda by remember { mutableStateOf(ajustes.ciudad) }
    var resultados by remember { mutableStateOf<List<Ciudad>>(emptyList()) }
    var buscando by remember { mutableStateOf(false) }
    var pinNuevo by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Ajustes", fontSize = 28.sp)

        Text("Ciudad para la temperatura: ${a.ciudad.ifEmpty { "(ninguna)" }}", fontSize = 18.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(busqueda, { busqueda = it }, label = { Text("Buscar ciudad") }, modifier = Modifier.weight(1f))
            Button(onClick = {
                buscando = true
                scope.launch { resultados = OpenMeteo.parsearCiudades(descargar(OpenMeteo.urlCiudad(busqueda))); buscando = false }
            }, enabled = busqueda.isNotBlank() && !buscando) { Text(if (buscando) "…" else "Buscar") }
        }
        resultados.forEach { c ->
            TextButton(onClick = { a = a.copy(ciudad = c.nombre, lat = c.lat, lon = c.lon); resultados = emptyList() }) {
                Text("${c.nombre}${c.region?.let { " ($it)" }.orEmpty()}", fontSize = 18.sp)
            }
        }

        Text("Tarjeta DIA", fontSize = 22.sp)
        TipoTarjeta.entries.forEach { t ->
            Row(Modifier.selectable(a.tarjetaTipo == t) { a = a.copy(tarjetaTipo = t) }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(a.tarjetaTipo == t, onClick = { a = a.copy(tarjetaTipo = t) })
                Text(when (t) { TipoTarjeta.QR -> "Código QR"; TipoTarjeta.BARRAS -> "Código de barras"; TipoTarjeta.APP -> "Abrir la app de DIA" })
            }
        }
        if (a.tarjetaTipo != TipoTarjeta.APP) OutlinedTextField(
            a.tarjetaNumero, { a = a.copy(tarjetaNumero = it.trim()) }, label = { Text("Número de la tarjeta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(a.nombreAyuda, { a = a.copy(nombreAyuda = it) }, label = { Text("Nombre de quien ayuda (sale en los errores)") }, modifier = Modifier.fillMaxWidth())

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(a.vozActivada, { a = a.copy(vozActivada = it) }); Spacer(Modifier.width(8.dp)); Text("Voz que anuncia las acciones")
        }

        OutlinedTextField(
            pinNuevo, { if (it.length <= 6 && it.all(Char::isDigit)) pinNuevo = it }, label = { Text("PIN nuevo (vacío = no cambiar)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation(),
            isError = pinNuevo.isNotEmpty() && !Pin.valido(pinNuevo), modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                var final = a.copy(nombreAyuda = a.nombreAyuda.ifBlank { "Fabri" })
                if (pinNuevo.isNotEmpty()) { val sal = Pin.nuevaSal(); final = final.copy(pinSal = sal, pinHash = Pin.hash(pinNuevo, sal)) }
                onGuardar(final)
            }, enabled = pinNuevo.isEmpty() || Pin.valido(pinNuevo)) { Text("Guardar") }
            OutlinedButton(onClick = onVolver) { Text("Volver") }
        }
    }
}
```

- [ ] **Step 7: Navegación del modo administrador**
  En `Pantalla.kt` añade:
```kotlin
    data object Pin : Pantalla
    data object Admin : Pantalla
    data object AdminFamiliares : Pantalla
    data class EditarFamiliar(val familiar: com.fabri.abuela.datos.Familiar?) : Pantalla
    data object AjustesAdmin : Pantalla
    data object Diagnostico : Pantalla
```
  En `AppAbuela`, debajo de los otros `val … by` / `remember`, añade:
```kotlin
    val scope = rememberCoroutineScope()
    val intentos = remember { com.fabri.abuela.seguridad.IntentosPin({ android.os.SystemClock.elapsedRealtime() }) }
    val toquesReloj = remember { com.fabri.abuela.seguridad.ContadorToques(reloj = { android.os.SystemClock.elapsedRealtime() }) }
```
  En `HomeScreen(…)` cambia `onToqueReloj = {}` por:
```kotlin
                onToqueReloj = { if (toquesReloj.toque()) pantalla = Pantalla.Pin },
```
  Cambia el `BackHandler` para que, dentro del modo administrador, ◁ vuelva al menú:
```kotlin
    BackHandler(enabled = true) {
        pantalla = when (pantalla) {
            is Pantalla.Confirmar -> Pantalla.Familia
            Pantalla.AdminFamiliares, Pantalla.AjustesAdmin, Pantalla.Diagnostico -> Pantalla.Admin
            is Pantalla.EditarFamiliar -> Pantalla.AdminFamiliares
            else -> Pantalla.Inicio
        }
    }
```
  Y añade estas ramas al `when (val p = pantalla)`:
```kotlin
        Pantalla.Pin -> com.fabri.abuela.ui.admin.PinScreen(ajustes, intentos, onOk = { pantalla = Pantalla.Admin }, onCancelar = volver)
        Pantalla.Admin -> com.fabri.abuela.ui.admin.AdminScreen(
            onFamiliares = { pantalla = Pantalla.AdminFamiliares },
            onAjustes = { pantalla = Pantalla.AjustesAdmin },
            onDiagnostico = { pantalla = Pantalla.Diagnostico },
            onSalirLauncher = {
                ctx.startActivity(android.content.Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            },
            onVolver = volver
        )
        Pantalla.AdminFamiliares -> com.fabri.abuela.ui.admin.AdminFamiliaresScreen(
            familiares,
            onEditar = { pantalla = Pantalla.EditarFamiliar(it) },
            onGuardarLista = { l -> scope.launch { repo.guardarFamiliares(l) } },
            onVolver = { pantalla = Pantalla.Admin }
        )
        is Pantalla.EditarFamiliar -> com.fabri.abuela.ui.admin.EditarFamiliarScreen(
            p.familiar,
            onGuardar = { nombre, tel, foto ->
                scope.launch {
                    val id = p.familiar?.id ?: java.util.UUID.randomUUID().toString()
                    val ruta = foto?.let { repo.copiarFoto(it, id) } ?: p.familiar?.rutaFoto
                    val nuevo = com.fabri.abuela.datos.Familiar(id, nombre, tel, ruta, p.familiar?.orden ?: familiares.size)
                    repo.guardarFamiliares(if (p.familiar == null) familiares + nuevo else familiares.map { if (it.id == id) nuevo else it })
                    pantalla = Pantalla.AdminFamiliares
                }
            },
            onCancelar = { pantalla = Pantalla.AdminFamiliares }
        )
        Pantalla.AjustesAdmin -> com.fabri.abuela.ui.admin.AjustesAdminScreen(
            ajustes,
            onGuardar = { nuevos -> scope.launch { repo.guardarAjustes(nuevos); pantalla = Pantalla.Admin } },
            onVolver = { pantalla = Pantalla.Admin }
        )
        Pantalla.Diagnostico -> Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Text("Diagnóstico (Tarea 19)", fontSize = 28.sp, modifier = Modifier.weight(1f))
            BotonVolver { pantalla = Pantalla.Admin }
        }
```
  Importa `kotlinx.coroutines.launch` con Alt+Enter.
  > Si la foto se cambia, `copiarFoto` sobrescribe `$id.jpg` y `FotoFamiliar` no se refresca, porque recuerda por `rutaFoto` y la ruta es la misma. Para evitarlo, en `Repositorio.copiarFoto` cambia el nombre del archivo a `"${id}_${System.currentTimeMillis()}.jpg"` y borra la foto anterior: añade el parámetro `anterior: String?` y haz `anterior?.let { File(it).delete() }` tras guardar. En la llamada de arriba pasa `repo.copiarFoto(it, id, p.familiar?.rutaFoto)`.

- [ ] **Step 8: Borrar los datos temporales y crear un PIN provisional**
  En `MainActivity.kt` borra el bloque `// TEMPORAL` entero. Como el asistente llega en la Tarea 18, para entrar ahora crea un PIN provisional poniendo temporalmente en su lugar:
```kotlin
        // TEMPORAL-2 — se borra en la Tarea 18
        lifecycleScope.launch {
            val a = repo.ajustes.first()
            if (a.pinHash.isEmpty()) { val sal = com.fabri.abuela.seguridad.Pin.nuevaSal()
                repo.guardarAjustes(a.copy(pinSal = sal, pinHash = com.fabri.abuela.seguridad.Pin.hash("1234", sal))) }
        }
```
  (Importa `kotlinx.coroutines.flow.first`.)
  > Como hay que desinstalar la app para que empiece limpia, en el Redmi: mantén pulsado el icono "Inicio Abuela" en el cajón de apps → Desinstalar. Después Run ▶ y vuelve a fijarla como pantalla de inicio.

- [ ] **Step 9: Probar**
  - [ ] Toca el reloj 5 veces rápido: aparece el teclado de PIN. 5 toques lentos no lo abren.
  - [ ] PIN 1234 → menú de administración. Cinco PIN incorrectos → "Espera 30 s".
  - [ ] Añade a 2 familiares con foto, edita uno, cámbialo de orden y borra uno. La pantalla FAMILIA lo refleja.
  - [ ] En Ajustes busca tu ciudad, elige el tipo de tarjeta y su número, desactiva la voz y cambia el PIN. Guarda y comprueba cada cosa en el inicio.
  - [ ] "Salir al launcher normal" abre los ajustes de la pantalla de inicio.

- [ ] **Step 10: Commit**
```powershell
git add .
git commit -m "feat: modo administrador con PIN, familiares y ajustes"
```

---

### Task 18: Asistente del primer arranque

**Files:**
- Create: `…/abuela/ui/admin/AsistenteScreen.kt`
- Modify: `…/abuela/navegacion/Pantalla.kt`, `…/abuela/navegacion/AppAbuela.kt`, `…/abuela/MainActivity.kt` (borrar `// TEMPORAL-2`)

**Interfaces:**
- Consumes: `TecladoPin`, `Pin`, `WhatsAppContactos.tienePermiso`, `Mensajes.tienePermiso`, `EditarFamiliarScreen`
- Produces:
  - `@Composable fun AsistenteScreen(hayFamiliares: Boolean, onPinCreado: (String) -> Unit, onAnadirFamiliar: () -> Unit, onTerminar: () -> Unit)`
  - `Pantalla.Asistente`
  - Al terminar, `Ajustes.configurado = true`.

- [ ] **Step 1: Asistente**
  `…/abuela/ui/admin/AsistenteScreen.kt`:
```kotlin
package com.fabri.abuela.ui.admin

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.fabri.abuela.acciones.WhatsAppContactos
import com.fabri.abuela.mensajes.Mensajes
import com.fabri.abuela.seguridad.Pin

@Composable
fun AsistenteScreen(hayFamiliares: Boolean, onPinCreado: (String) -> Unit, onAnadirFamiliar: () -> Unit, onTerminar: () -> Unit) {
    val ctx = LocalContext.current
    var paso by rememberSaveable { mutableIntStateOf(0) }
    var primerPin by rememberSaveable { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var contactosOk by remember { mutableStateOf(WhatsAppContactos.tienePermiso(ctx)) }
    var notifOk by remember { mutableStateOf(Mensajes.tienePermiso(ctx)) }
    val pedirContactos = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { contactosOk = it }
    LifecycleResumeEffect(Unit) { notifOk = Mensajes.tienePermiso(ctx); onPauseOrDispose { } }

    when (paso) {
        0 -> TecladoPin(if (primerPin == null) "Crea un PIN (4-6 números)" else "Repite el PIN", mensaje, onCompleto = { p ->
            when {
                primerPin == null && !Pin.valido(p) -> mensaje = "Tienen que ser de 4 a 6 números"
                primerPin == null -> { primerPin = p; mensaje = null }
                p == primerPin -> { onPinCreado(p); paso = 1 }
                else -> { primerPin = null; mensaje = "No coinciden. Empieza otra vez" }
            }
        }, onCancelar = null)
        else -> Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Configuración inicial", fontSize = 28.sp)
            Text("1. Contactos (para la videollamada directa) ${if (contactosOk) "✅" else ""}", fontSize = 20.sp)
            if (!contactosOk) Button(onClick = { pedirContactos.launch(Manifest.permission.READ_CONTACTS) }) { Text("Dar permiso") }
            Text("2. Acceso a notificaciones (para ver los mensajes nuevos) ${if (notifOk) "✅" else ""}", fontSize = 20.sp)
            if (!notifOk) Button(onClick = {
                ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }) { Text("Abrir ajustes y activar \"Inicio Abuela - mensajes\"") }
            Text("3. Al menos un familiar ${if (hayFamiliares) "✅" else ""}", fontSize = 20.sp)
            Button(onClick = onAnadirFamiliar) { Text("Añadir familiar") }
            Text("La ciudad y la tarjeta DIA se configuran después en Ajustes.", fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            Button(onClick = onTerminar, enabled = hayFamiliares, modifier = Modifier.fillMaxWidth().height(64.dp)) { Text("Terminar", fontSize = 22.sp) }
        }
    }
}
```
  Si `LifecycleResumeEffect` sale en rojo, añade la dependencia `implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")` en `app/build.gradle.kts` y pulsa Sync.

- [ ] **Step 2: Conectar**
  - En `Pantalla.kt` añade `data object Asistente : Pantalla`.
  - En `AppAbuela`, el primer arranque debe abrir el asistente. Añade `val ajustesCargados by repo.ajustes.map<Ajustes, Ajustes?> { it }.collectAsState(initial = null)` (importa `kotlinx.coroutines.flow.map`).
  - Justo debajo de los `LaunchedEffect` existentes añade:
```kotlin
    LaunchedEffect(ajustesCargados?.configurado) {
        if (ajustesCargados?.configurado == false && pantalla !is Pantalla.EditarFamiliar) pantalla = Pantalla.Asistente
    }
```
  - Cambia el `LaunchedEffect(irAInicio)` para que no saque del asistente:
    `LaunchedEffect(irAInicio) { if (ajustes.configurado) pantalla = Pantalla.Inicio }`
  - En el `BackHandler`, la rama `else ->` pasa a ser `else -> if (ajustes.configurado) Pantalla.Inicio else Pantalla.Asistente`.
  - Añade la rama:
```kotlin
        Pantalla.Asistente -> com.fabri.abuela.ui.admin.AsistenteScreen(
            hayFamiliares = familiares.isNotEmpty(),
            onPinCreado = { pin ->
                scope.launch {
                    val sal = com.fabri.abuela.seguridad.Pin.nuevaSal()
                    repo.guardarAjustes(ajustes.copy(pinSal = sal, pinHash = com.fabri.abuela.seguridad.Pin.hash(pin, sal)))
                }
            },
            onAnadirFamiliar = { pantalla = Pantalla.EditarFamiliar(null) },
            onTerminar = { scope.launch { repo.guardarAjustes(ajustes.copy(configurado = true)); pantalla = Pantalla.Inicio } }
        )
```
  - En la rama `is Pantalla.EditarFamiliar`, tras guardar y al cancelar, vuelve a `if (ajustes.configurado) Pantalla.AdminFamiliares else Pantalla.Asistente` en lugar de `Pantalla.AdminFamiliares`.
  > El asistente reaparece en el paso 0 al volver de "Añadir familiar", porque `paso` se reinicia al salir de la composición. Para evitarlo, `AsistenteScreen` debe empezar en el paso 1 si ya hay PIN. Añade el parámetro `yaHayPin: Boolean` y usa `rememberSaveable { mutableIntStateOf(if (yaHayPin) 1 else 0) }`. En la llamada pasa `yaHayPin = ajustes.pinHash.isNotEmpty()`.

- [ ] **Step 3: Quitar el PIN provisional**
  Borra el bloque `// TEMPORAL-2` de `MainActivity.kt`. Comprueba con `Ctrl+Shift+F` que ya no queda "TEMPORAL" en el proyecto.

- [ ] **Step 4: Probar desde cero**
  Desinstala la app, Run ▶ y fíjala como pantalla de inicio.
  - [ ] Sale el asistente: crear PIN → repetir PIN; si no coinciden, se reinicia.
  - [ ] Dar permiso de contactos → ✅.
  - [ ] Abrir los ajustes de notificaciones, activar el acceso y volver → ✅.
  - [ ] Añadir familiar → vuelve al asistente con ✅. "Terminar" solo se activa cuando hay un familiar.
  - [ ] Pulsar ○ durante el asistente no lo salta.
  - [ ] Tras terminar, al reiniciar el móvil arranca directamente en el inicio.

- [ ] **Step 5: Commit**
```powershell
git add .
git commit -m "feat: asistente de primer arranque con permisos"
```

---

### Task 19: Diagnóstico ("Comprobar botones")

**Files:**
- Create: `…/abuela/admin/Diagnostico.kt`, `…/abuela/ui/admin/DiagnosticoScreen.kt`
- Modify: `…/abuela/navegacion/AppAbuela.kt`

**Interfaces:**
- Consumes: `Paquetes`, `Lanzador.PALABRAS_VOZ`, `WhatsAppContactos`, `Mensajes`, `Ajustes`, `Familiar`, `TiempoCache`, `Controlador`
- Produces:
  - `data class Comprobacion(nombre: String, ok: Boolean, ayuda: String)`
  - `object Diagnostico { fun comprobar(ctx: Context, ajustes: Ajustes, familiares: List<Familiar>, tiempo: TiempoCache?): List<Comprobacion> }`
  - `@Composable fun DiagnosticoScreen(comprobaciones: List<Comprobacion>, controlador: Controlador, familiares: List<Familiar>, onVolver: () -> Unit)`

- [ ] **Step 1: Comprobaciones**
  `…/abuela/admin/Diagnostico.kt`:
```kotlin
package com.fabri.abuela.admin

import android.app.role.RoleManager
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import com.fabri.abuela.acciones.Lanzador
import com.fabri.abuela.acciones.Paquetes
import com.fabri.abuela.acciones.WhatsAppContactos
import com.fabri.abuela.datos.Ajustes
import com.fabri.abuela.datos.Familiar
import com.fabri.abuela.datos.TiempoCache
import com.fabri.abuela.datos.TipoTarjeta
import com.fabri.abuela.mensajes.Mensajes

data class Comprobacion(val nombre: String, val ok: Boolean, val ayuda: String)

object Diagnostico {
    fun comprobar(ctx: Context, ajustes: Ajustes, familiares: List<Familiar>, tiempo: TiempoCache?): List<Comprobacion> {
        val pm = ctx.packageManager
        fun instalada(p: String) = pm.getLaunchIntentForPackage(p) != null
        val esInicio = ctx.getSystemService(RoleManager::class.java)?.isRoleHeld(RoleManager.ROLE_HOME) == true
        val atajoVoz = runCatching {
            val la = ctx.getSystemService(LauncherApps::class.java)
            val q = LauncherApps.ShortcutQuery().setPackage(Paquetes.CHATGPT).setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
            la.getShortcuts(q, Process.myUserHandle()).orEmpty().any { s ->
                val t = "${s.id} ${s.shortLabel} ${s.longLabel}".lowercase(); Lanzador.PALABRAS_VOZ.any { it in t }
            }
        }.getOrDefault(false)

        val lista = mutableListOf(
            Comprobacion("Pantalla de inicio predeterminada", esInicio, "Ajustes → Apps → Apps predeterminadas → Pantalla de inicio"),
            Comprobacion("WhatsApp instalado", instalada(Paquetes.WHATSAPP), "Instala WhatsApp"),
            Comprobacion("YouTube instalado", instalada(Paquetes.YOUTUBE), "Instala YouTube"),
            Comprobacion("ChatGPT instalado", instalada(Paquetes.CHATGPT), "Instala ChatGPT e inicia sesión"),
            Comprobacion("Atajo de voz de ChatGPT", atajoVoz, "Se abrirá ChatGPT normal (plan B)"),
            Comprobacion("Permiso de contactos", WhatsAppContactos.tienePermiso(ctx), "Ajustes → Apps → Inicio Abuela → Permisos"),
            Comprobacion("Acceso a notificaciones", Mensajes.tienePermiso(ctx), "Ajustes → Acceso a notificaciones → Inicio Abuela - mensajes"),
            Comprobacion("Lector de mensajes conectado", Mensajes.conectado, "Activa Inicio automático y batería sin restricciones"),
            Comprobacion(
                "Tarjeta DIA configurada",
                if (ajustes.tarjetaTipo == TipoTarjeta.APP) instalada(Paquetes.DIA) else ajustes.tarjetaNumero.isNotBlank(),
                "Ajustes → Tarjeta DIA"
            ),
            Comprobacion("Ciudad configurada", ajustes.lat != null, "Ajustes → Buscar ciudad"),
            Comprobacion("Temperatura recibida", tiempo != null, "Revisa la conexión a internet")
        )
        familiares.forEach { f ->
            lista += Comprobacion(
                "Videollamada directa: ${f.nombre}",
                WhatsAppContactos.buscarVideollamada(ctx, f.telefono) != null,
                "Guarda ${f.telefono} en Contactos y abre WhatsApp para que sincronice"
            )
        }
        return lista
    }
}
```

- [ ] **Step 2: Pantalla**
  `…/abuela/ui/admin/DiagnosticoScreen.kt`:
```kotlin
package com.fabri.abuela.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabri.abuela.acciones.Accion
import com.fabri.abuela.admin.Comprobacion
import com.fabri.abuela.datos.Familiar
import com.fabri.abuela.navegacion.Controlador
import com.fabri.abuela.ui.components.BotonVolver

@Composable
fun DiagnosticoScreen(comprobaciones: List<Comprobacion>, controlador: Controlador, familiares: List<Familiar>, onVolver: () -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Comprobaciones", fontSize = 26.sp) }
            items(comprobaciones) { c ->
                Column {
                    Text("${if (c.ok) "✅" else "❌"} ${c.nombre}", fontSize = 20.sp)
                    if (!c.ok) Text("   → ${c.ayuda}", fontSize = 16.sp)
                }
            }
            item { Text("Probar acciones", fontSize = 26.sp) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { controlador.pulsar(Accion.Camara, "Probando cámara") }) { Text("Cámara") }
                    OutlinedButton(onClick = { controlador.pulsar(Accion.YouTube, "Probando YouTube") }) { Text("YouTube") }
                    OutlinedButton(onClick = { controlador.pulsar(Accion.ChatGptVoz, "Probando ChatGPT") }) { Text("ChatGPT voz") }
                    OutlinedButton(onClick = { controlador.pulsar(Accion.AbrirWhatsApp, "Probando WhatsApp") }) { Text("WhatsApp") }
                    familiares.forEach { f ->
                        OutlinedButton(onClick = { controlador.pulsar(Accion.WhatsAppChat(f.telefono), "Probando chat") }) { Text("Chat con ${f.nombre}") }
                    }
                }
            }
        }
        BotonVolver(onVolver)
    }
}
```
  > La videollamada no tiene botón de prueba aquí, para no llamar a nadie sin querer. Ya aparece arriba si su fila de contacto existe.

- [ ] **Step 3: Conectar**
  En `AppAbuela`, sustituye la rama provisional `Pantalla.Diagnostico -> Column { … }` por:
```kotlin
        Pantalla.Diagnostico -> {
            val lista by produceState(emptyList<com.fabri.abuela.admin.Comprobacion>(), ajustes, familiares, tiempo) {
                value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.fabri.abuela.admin.Diagnostico.comprobar(ctx, ajustes, familiares, tiempo)
                }
            }
            com.fabri.abuela.ui.admin.DiagnosticoScreen(lista, controlador, familiares) { pantalla = Pantalla.Admin }
        }
```

- [ ] **Step 4: Probar**
  - [ ] Todas las comprobaciones salen en ✅ menos las que de verdad faltan.
  - [ ] Quita el acceso a notificaciones: "Acceso a notificaciones" sale en ❌ con su ayuda. Después vuelve a activarlo.
  - [ ] Los botones de "Probar acciones" abren cada app.

- [ ] **Step 5: Tests y commit**
  Run: `.\gradlew.bat testDebugUnitTest` → Expected: PASS (todos).
```powershell
git add .
git commit -m "feat: pantalla de diagnóstico"
```

---

## FASE 5 — Entrega

### Task 20: Guía de instalación, checklist y prueba con la abuela

**Files:**
- Create: `docs/guia-instalacion.md`

**Interfaces:**
- Consumes: todo lo anterior.
- Produces: la app instalada y configurada en el Redmi, y la checklist del spec (§7.3) completada.

- [ ] **Step 1: Escribir la guía**
  `docs/guia-instalacion.md`:
```markdown
# Instalar "Inicio Abuela" en el Redmi

## Primera vez
1. Opciones de desarrollador: Depuración USB + Instalar vía USB (ver plan, Tarea 1).
2. Android Studio → Run ▶ con el móvil conectado.
3. Ajustes → Pantalla de inicio → Navegación del sistema → **Botones**.
4. Ajustes → Apps → Apps predeterminadas → **Pantalla de inicio → Inicio Abuela**.
5. Ajustes → Apps → Administrar apps → Inicio Abuela:
   - **Inicio automático: activado**
   - **Ahorro de batería: Sin restricciones**
   - Otros permisos → "Mostrar ventanas emergentes mientras se ejecuta en segundo plano": **permitir**
6. Abre Seguridad → Aumentar velocidad / limpieza y añade Inicio Abuela a la lista de excepciones (que no la cierre).
7. Completa el asistente: PIN, contactos, notificaciones, familiares.
8. Modo administrador (5 toques en el reloj) → Ajustes: ciudad, tarjeta DIA, nombre de ayuda.
9. Modo administrador → Comprobar botones: todo en ✅.
10. Cada familiar tiene que estar guardado en **Contactos** con el mismo número y con WhatsApp.

## Actualizar
Conectar por USB → Run ▶. Los datos (familiares, ajustes) se conservan.

## Si algo va mal
- **Volver al launcher de Xiaomi:** modo administrador → "Salir al launcher normal".
- **Olvidé el PIN:** desinstalar y reinstalar (se pierde la configuración).
- **No salen los mensajes nuevos:** Comprobar botones → "Lector de mensajes conectado". Repetir el paso 5.
```

- [ ] **Step 2: Checklist manual completa**
  Con el móvil configurado como lo usará ella, recorre y marca **cada punto de la §7.3 del spec**:
  - [ ] Se puede elegir como pantalla de inicio, y ○ vuelve a ella desde cualquier app.
  - [ ] Cada botón funciona, comprobado también con el Diagnóstico.
  - [ ] La videollamada se abre directamente con cada familiar.
  - [ ] El QR de DIA se lee en la caja real.
  - [ ] Aparece la temperatura y, sin red, se mantiene el último valor.
  - [ ] Tras reiniciar el móvil arranca en el launcher.
  - [ ] Tras un día sin tocarlo, los mensajes nuevos siguen apareciendo (HyperOS no ha cerrado el lector).
  - [ ] Texto, audio y grupo aparecen bien, el 🔴 sale en el familiar correcto y desaparecen tras leerlos.
  - [ ] "Abrir WhatsApp" abre la lista de chats.

- [ ] **Step 3: Prueba con la abuela**
  Sin ayudarla, pídele una a una que:
  1. llame por videollamada a un familiar;
  2. abra los mensajes de un familiar;
  3. mire quién le ha escrito y abra ese chat;
  4. haga una foto;
  5. abra YouTube;
  6. le pregunte algo a ChatGPT hablando;
  7. enseñe la tarjeta DIA.

  Apunta en `docs/guia-instalacion.md`, en una sección "Prueba con la abuela (fecha)", dónde dudó o tocó algo sin querer. Cada duda es una mejora candidata para una versión 2. Esas mejoras no se hacen en este plan.

- [ ] **Step 4: Commit final**
```powershell
git add .
git commit -m "docs: guía de instalación y resultados de la prueba"
```
