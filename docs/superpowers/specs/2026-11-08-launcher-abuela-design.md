# Launcher para la abuela — Documento de diseño

**Fecha:** 2026-11-08
**Estado:** pendiente de revisión
**Autor:** Fabri (con Claude)

---

## 1. Objetivo

Crear una pantalla de inicio (launcher) para Android que permita a una persona de ~80 años usar su móvil sin perderse ni tocar cosas por error. Cada botón lleva directamente a la acción final (llamar, abrir un chat, hablar con ChatGPT, enseñar la tarjeta DIA), saltándose los menús de las otras apps.

### Problemas que resuelve (según el usuario)
- **Toques sin querer:** abre menús, mueve o borra iconos, cambia ajustes.
- **Se pierde dentro de las apps:** sabe abrir WhatsApp, pero no llegar a la llamada o el chat de una persona.

### Criterios de éxito
1. Desde la pantalla de inicio, la abuela hace cualquiera de estas acciones con **1 toque + 1 confirmación como máximo**: videollamada a un familiar, abrir el chat de un familiar, hacer una foto, abrir YouTube, hablar por voz con ChatGPT, enseñar el QR de la tarjeta DIA en caja, ver quién le ha escrito y abrir ese chat.
2. Un toque accidental no puede dejarla en una pantalla de la que no sepa salir. El botón ○ del sistema **siempre** vuelve al inicio.
3. No puede cambiar la configuración del launcher sin el PIN del administrador.
4. Nunca ve un mensaje de error técnico.

### Fuera de alcance (v1)
- Publicar en Google Play: se instala por cable (APK).
- Soporte remoto: el administrador vive con ella.
- Cambiar la interfaz de dentro de WhatsApp, YouTube, ChatGPT o DIA. Android no lo permite.
- Galería de fotos, llamadas de teléfono normales, SOS, linterna, recordatorios de medicación. Se podrán añadir en el futuro si hacen falta.
- Atajos a vídeos o canales concretos de YouTube.

---

## 2. Contexto y restricciones

| Aspecto | Valor |
|---|---|
| Dispositivo | Xiaomi Redmi (HyperOS / MIUI), Android 10 o superior |
| Usuario final | Abuela, ~80 años, español |
| Administrador | Fabri, convive con ella; **no sabe Kotlin/Android** (el plan parte de cero) |
| Distribución | APK instalado por USB desde Android Studio |
| Apps de terceros | WhatsApp (`com.whatsapp`), YouTube (`com.google.android.youtube`), ChatGPT (`com.openai.chatgpt`), DIA (paquete a confirmar en la fase 0) |

### Particularidades del Redmi
- **Gestos:** con un launcher que no sea el de Xiaomi, la navegación por gestos a pantalla completa puede no funcionar. **Decisión:** usar la navegación con 3 botones (◁ ○ □), que además es más fácil para ella.
- **Batería:** HyperOS cierra apps en segundo plano de forma agresiva. **Decisión:** activar "Inicio automático" y poner el ahorro de batería en "Sin restricciones" para el launcher. Se documenta en la guía de instalación.
- **Fijar como inicio:** Ajustes → Apps → Apps predeterminadas → Pantalla de inicio.

---

## 3. Stack

- **Lenguaje:** Kotlin
- **Interfaz:** Jetpack Compose (Material 3) con un tema propio de alto contraste
- **Persistencia:** Jetpack DataStore (Preferences + JSON serializado con `kotlinx.serialization`)
- **QR / código de barras:** ZXing (`com.google.zxing:core`)
- **Tiempo:** API de Open-Meteo (gratis, sin clave), con `HttpURLConnection` o Ktor client
- **Voz:** `android.speech.tts.TextToSpeech` (es-ES)
- **Tests:** JUnit 4 + tests de Compose UI para lo crítico
- **minSdk:** 29 (Android 10) · **targetSdk:** el último estable
- **Alternativas descartadas:**
  - Flutter o React Native: no aportan nada si solo es Android, y las funciones de launcher necesitan código nativo de todas formas.
  - Launchers ya hechos: no permiten acciones directas como la videollamada o el modo voz.

---

## 4. Experiencia de usuario

### 4.1 Principios
- **Vertical fijo.** Sin scroll en la pantalla de inicio.
- **Cada botón dice lo que hace:** icono, título grande y una frase corta ("Llamar y mensajes").
- **Tamaño:** mínimo 120dp de alto por botón y 16dp de separación entre ellos. Texto de 28sp o más en los títulos.
- **Alto contraste:** fondo claro, botones de colores sólidos distintos para cada función y texto oscuro.
- **Sin gestos:** no hay pulsación larga, deslizamientos, menús contextuales ni widgets que se puedan mover.
- **Respuesta clara a cada toque:** vibración corta y una voz que dice lo que va a pasar ("Abriendo la cámara"). La voz se puede desactivar en el modo administrador.
- **Toques repetidos:** se ignoran los toques dobles y rápidos. Tras lanzar una acción, el resto de botones se bloquea 1,5 s.

### 4.2 Pantalla de inicio
```
┌─────────────────────────────┐
│  10:45          🌡️ 14°C ☁️   │
│  Domingo, 8 de noviembre    │
├──────────────┬──────────────┤
│ 👨‍👩‍👧 FAMILIA   │  📷 CÁMARA   │
│ Llamar y     │  Hacer fotos │
│ mensajes     │              │
├──────────────┼──────────────┤
│ ▶️ YOUTUBE    │ 💬 PREGUNTAR │
│ Ver vídeos   │ Hablar con   │
│              │ ChatGPT      │
├──────────────┴──────────────┤
│   🛒  MI TARJETA DIA        │
│   Enseñar en la caja        │
└─────────────────────────────┘
```
- **Reloj y fecha:** formato largo en español.
- **Temperatura:** temperatura actual e icono del cielo de la ciudad configurada. Se actualiza cada 30 minutos mientras la pantalla está visible. Sin conexión, muestra el último valor guardado. Si nunca ha habido datos, la temperatura no aparece (no se muestra "--" ni un error).
- **Botón atrás (◁):** en la pantalla de inicio no hace nada.

### 4.3 Pantalla Familia
```
┌─────────────────────────────┐
│  📩 MENSAJES NUEVOS          │
│  ┌───────────────────────┐  │
│  │ (foto) Fabri          │  │
│  │ ¿Has comido ya?       │  │
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │ (foto) María          │  │
│  │ 🎤 Mensaje de voz     │  │
│  └───────────────────────┘  │
├─────────────────────────────┤
│  (foto) FABRI  🔴 1 nuevo    │
│  [📹 VIDEOLLAMADA][🎤 MENSAJES]│
│  …                          │
├─────────────────────────────┤
│  💬 ABRIR WHATSAPP           │
│  ◀ VOLVER AL INICIO          │
└─────────────────────────────┘
```
**Sección "📩 Mensajes nuevos" (arriba):**
- Muestra los mensajes de WhatsApp **sin leer** que tiene en las notificaciones. Hay una tarjeta grande por conversación con la foto (si el remitente es un familiar configurado) o la inicial, el nombre y el último mensaje en texto grande.
- **Audios y otros tipos:** los audios aparecen como "🎤 Mensaje de voz". Las fotos como "📷 Foto" y los vídeos como "🎬 Vídeo".
- **Al tocar una tarjeta** se abre ese chat en WhatsApp (ver 5.3). Si el remitente no es un familiar configurado, se abre WhatsApp en la lista de chats.
- **Si no hay mensajes nuevos**, la sección se reduce a una línea "No tienes mensajes nuevos", sin dejar huecos.
- **Grupos:** se muestran con el nombre del grupo y quién escribió ("Familia · Fabri: …").
- **Sin lectura en voz alta** de los mensajes (decisión del usuario).

**Aviso en cada familiar:** "🔴 N nuevos" junto al nombre si hay mensajes sin leer suyos.

**Botón "💬 ABRIR WHATSAPP"** al final de la lista: abre WhatsApp normal, para ver chats antiguos.

**Familiares:**
- Una tarjeta grande por familiar con su foto (circular, ~96dp), su nombre en grande y dos botones a lo ancho:
  - **📹 VIDEOLLAMADA** (verde)
  - **🎤 MENSAJES** (azul)
- **Scroll vertical** solo si hay más de 3 familiares.
- **Volver:** botón fijo abajo **◀ VOLVER AL INICIO**. El botón ◁ también vuelve.

### 4.4 Confirmación de llamada
Antes de la videollamada aparece un diálogo a pantalla completa:
> **¿Llamar a Fabri?** (con su foto)
> [ **SÍ, LLAMAR** ] (verde, grande) [ **NO** ] (rojo, grande)

Abrir un chat de mensajes **no** pide confirmación, porque no tiene consecuencias.

### 4.5 Pantalla Tarjeta DIA
- QR o código de barras a pantalla completa sobre fondo blanco.
- El **brillo** sube al máximo mientras la pantalla está abierta (`WindowManager.LayoutParams.screenBrightness = 1f`) y vuelve al normal al salir.
- La pantalla no se apaga mientras está abierta (`FLAG_KEEP_SCREEN_ON`).
- Botón **◀ VOLVER AL INICIO**.

### 4.6 Pantalla de error
Si una acción falla:
> 😕 **No se ha podido abrir.**
> Pídele ayuda a Fabri.
> [ ◀ VOLVER AL INICIO ]

El nombre que aparece en "Pídele ayuda a ___" se puede configurar.

### 4.7 Modo administrador
- **Entrada:** tocar el reloj 5 veces en menos de 3 segundos y poner el PIN de 4 a 6 dígitos.
- **Qué se puede hacer:**
  - Familiares: añadir, editar, borrar y reordenar. Cada uno tiene nombre, teléfono con prefijo (+34…) y foto (de la galería, copiada dentro de la app).
  - Tarjeta DIA: número y tipo de código (QR o código de barras), o elegir "abrir la app de DIA".
  - Ciudad para la temperatura: se busca por nombre con la geocodificación de Open-Meteo.
  - Nombre de la persona de ayuda, activar o desactivar la voz, y cambiar el PIN.
  - **Comprobar botones:** una pantalla que prueba cada acción y dice si funciona.
  - **Salir al launcher normal:** abre los ajustes de "Pantalla de inicio predeterminada".
- **Primer arranque:** un asistente obliga a crear el PIN y a añadir al menos un familiar.

---

## 5. Arquitectura

### 5.1 Módulos y paquetes
Un único módulo de app `app`, con paquete `com.fabri.abuela`.

```
com.fabri.abuela
├── MainActivity.kt          ← actividad HOME (launcher), aloja la navegación Compose
├── ui/
│   ├── theme/               ← colores, tipografía, tamaños de alto contraste
│   ├── components/          ← BotonGrande, TarjetaFamiliar, DialogoConfirmar, BotonVolver
│   ├── home/                ← HomeScreen + HomeViewModel (reloj, tiempo)
│   ├── familia/             ← FamiliaScreen
│   ├── dia/                 ← TarjetaDiaScreen
│   ├── error/               ← ErrorScreen
│   └── admin/               ← PinScreen, AdminScreen, EditarFamiliarScreen, AsistenteInicial, DiagnosticoScreen
├── acciones/
│   ├── Accion.kt            ← sealed interface: Camara, YouTube, ChatGptVoz, WhatsAppChat(tel), WhatsAppVideo(tel), TarjetaDia
│   ├── Lanzador.kt          ← ejecuta una Accion; devuelve Resultado (Ok | PlanB usado | Error)
│   ├── WhatsAppContactos.kt ← busca en ContactsContract las filas de videollamada de WhatsApp
│   └── AtajosApps.kt        ← busca y lanza atajos con LauncherApps
├── datos/
│   ├── Familiar.kt          ← data class (id, nombre, telefono, rutaFoto, orden)
│   ├── Ajustes.kt           ← data class (pinHash, ciudad, lat, lon, tarjetaDia, voz, nombreAyuda)
│   └── Repositorio.kt       ← lectura y escritura con DataStore
├── mensajes/
│   ├── LectorNotificaciones.kt ← NotificationListenerService: filtra com.whatsapp y publica la lista
│   ├── MensajeNuevo.kt      ← data class (conversacion, remitente, texto, tipo, esGrupo, hora)
│   └── MensajesRepo.kt      ← StateFlow<List<MensajeNuevo>> en memoria + asociación con Familiar por nombre/teléfono
├── tiempo/
│   └── OpenMeteo.kt         ← pide la temperatura actual y la guarda en caché
└── feedback/
    ├── Voz.kt               ← envoltorio de TextToSpeech
    └── Antirrebote.kt       ← bloqueo de 1,5 s tras cada acción
```

**Responsabilidades:**
- **UI:** solo pinta las pantallas y emite `Accion`.
- **`Lanzador`:** es el único que conoce intents y paquetes.
- **`Repositorio`:** es el único que conoce DataStore.

### 5.2 Manifest
- `MainActivity` con:
  - `intent-filter` MAIN + `CATEGORY_HOME` + `CATEGORY_DEFAULT`
  - `launchMode="singleTask"`, `screenOrientation="portrait"`, `stateNotNeeded="true"`
- **Permisos:**
  - `INTERNET`: para la temperatura.
  - `READ_CONTACTS`: para la videollamada de WhatsApp. Se pide en el asistente inicial con una explicación.
  - `VIBRATE`.
- **Servicio `LectorNotificaciones`:** declarado con `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`. El acceso a las notificaciones se concede a mano una vez (Ajustes → Acceso a notificaciones). El asistente inicial lleva directamente a esa pantalla.
- `<queries>` con los paquetes de WhatsApp, YouTube, ChatGPT y DIA, y con el intent de cámara. Hace falta por la visibilidad de paquetes de Android 11 o superior.

### 5.3 Cómo se ejecuta cada acción

| Acción | Intento principal | Plan B | Plan C |
|---|---|---|---|
| **Cámara** | `MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA` | `getLaunchIntentForPackage` de la cámara por defecto | Error |
| **YouTube** | `getLaunchIntentForPackage("com.google.android.youtube")` | — | Error "no instalado" |
| **ChatGPT voz** | `LauncherApps.getShortcuts(package=com.openai.chatgpt)` → el atajo cuyo nombre contenga "voice" o "voz" → `startShortcut()` (se permite porque somos el launcher por defecto) | Abrir ChatGPT normal | Error |
| **WhatsApp chat** | `ACTION_VIEW` `https://wa.me/<tel sin +>` con `setPackage("com.whatsapp")` | `getLaunchIntentForPackage("com.whatsapp")` | Error |
| **WhatsApp videollamada** | Buscar en `ContactsContract.Data` la fila con `MIMETYPE = "vnd.android.cursor.item/vnd.com.whatsapp.video.call"` cuyo contacto tenga ese teléfono (normalizado) → `ACTION_VIEW` con `ContentUris.withAppendedId(Data.CONTENT_URI, id)` y ese mimetype, `setPackage("com.whatsapp")` | Abrir el chat (fila anterior) | Error |
| **Abrir WhatsApp** | `getLaunchIntentForPackage("com.whatsapp")` | — | Error "no instalado" |
| **Abrir mensaje nuevo** | Lanzar el `contentIntent` (PendingIntent) de la notificación, que abre ese chat exacto | Si el remitente es un familiar: WhatsApp chat por teléfono | Abrir WhatsApp |
| **Tarjeta DIA** | Modo "código fijo": generar QR o código de barras con ZXing a partir del número guardado y mostrar `TarjetaDiaScreen` | Modo "app DIA": abrir la app de DIA (atajo de tarjeta si existe, si no la pantalla principal) | Error |

**Requisito para la videollamada:** cada familiar debe estar guardado en los contactos del móvil con el mismo número que en el launcher, y WhatsApp debe haber sincronizado los contactos. El diagnóstico del modo administrador avisa si falta.

### 5.4 Flujo de datos
```
Toque → Antirrebote (¿bloqueado? → ignorar)
      → Voz.decir("Abriendo …") + vibración
      → [si es videollamada] DialogoConfirmar → NO → volver
      → Lanzador.ejecutar(accion)
          → Ok / PlanB → la otra app pasa a primer plano
          → Error → navegar a ErrorScreen
```
Las acciones de mostrar algo (Familia, Tarjeta DIA en modo fijo) son navegación interna de Compose y no salen de la app.

### 5.5 Mensajes nuevos (lectura de notificaciones)
- **Qué lee:** `LectorNotificaciones` recibe `onNotificationPosted` y `onNotificationRemoved`, y solo atiende las de `com.whatsapp`. Descarta la notificación resumen del grupo (`FLAG_GROUP_SUMMARY`) y las que no son de mensajes (llamadas en curso, copias de seguridad).
- **Cómo saca los datos:** usa `NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification()`, que da el nombre de la conversación, si es grupo, y los mensajes con remitente y texto. Si no viene en ese formato, usa `EXTRA_TITLE` y `EXTRA_TEXT`.
- **Tipo de mensaje:** se deduce del texto que pone WhatsApp ("🎤 Mensaje de voz", "📷 Foto"…). Se normaliza a nuestras etiquetas y, si no se reconoce, se muestra el texto tal cual.
- **Asociación con familiares:** se busca el familiar por nombre de contacto, comparando sin acentos ni mayúsculas con el nombre de la agenda del móvil para ese teléfono. Si no hay coincidencia, se trata como "otro".
- **Al arrancar:** se cargan las notificaciones ya existentes con `getActiveNotifications()` en `onListenerConnected`.
- **Privacidad:** la lista vive **solo en memoria**. Nunca se guarda en disco ni sale del móvil.
- **Sin permiso concedido:** la sección muestra un aviso pequeño, visible solo en el modo administrador; la abuela no ve nada roto. El Diagnóstico lo marca en rojo.
- **HyperOS:** el servicio necesita "Inicio automático" y batería "Sin restricciones" (ver sección 2). Si Android lo desconecta, se pide la reconexión con `requestRebind()` al volver a la pantalla de inicio.

### 5.6 Tiempo (Open-Meteo)
- **Pronóstico:** `GET https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current=temperature_2m,weather_code&timezone=auto`
- **Ciudad:** `GET https://geocoding-api.open-meteo.com/v1/search?name={ciudad}&language=es&count=5` (solo en el modo administrador)
- **Caché:** el último valor y su hora se guardan en DataStore. Se refresca al volver a la pantalla de inicio si tiene más de 30 minutos.
- **Icono:** se elige del `weather_code` con una tabla sencilla (☀️ ⛅ ☁️ 🌧️ ⛈️ ❄️ 🌫️).

### 5.7 Seguridad del PIN
- Se guarda como hash: SHA-256 con una sal aleatoria guardada aparte.
- Tras 5 fallos seguidos, 30 segundos de espera.
- Si se olvida el PIN, se recupera desinstalando y reinstalando la app, porque el administrador tiene acceso físico.

---

## 6. Gestión de errores
- **Nada técnico a la vista:** ninguna excepción llega a la interfaz. `Lanzador` captura `ActivityNotFoundException`, `SecurityException` e `IllegalStateException` y devuelve `Error`.
- **Datos que fallan:** si la foto de un familiar no carga, se muestra su inicial sobre un círculo de color. Si DataStore falla al leer, se usan valores por defecto y se registra en Logcat.
- **Sin internet:** solo afecta a la temperatura, que se queda con el valor en caché.
- **Si se cierra el launcher:** al ser la pantalla de inicio, Android lo reabre al pulsar ○.

---

## 7. Pruebas

### 7.1 Automáticas (JUnit)
- `Lanzador`: para cada `Accion`, con un `PackageManager` simulado, que se elige el intent correcto y que se cae al plan B o C cuando falta la app o el atajo.
- `WhatsAppContactos`: normalización de teléfonos (+34 612 345 678 = 612345678 = 0034612345678).
- `Antirrebote`: se ignoran los toques dentro de la ventana de 1,5 s.
- Mapeo de `weather_code` a icono y parseo del JSON de Open-Meteo.
- Hash y verificación del PIN, y el bloqueo tras 5 fallos.
- `MensajesRepo`: a partir de notificaciones de ejemplo (chat individual, grupo, audio, foto, resumen de grupo), que la lista resultante y la asociación con familiares son correctas, y que al quitar la notificación desaparece de la lista.

### 7.2 Compose UI
- `HomeScreen` muestra los 5 botones y responde al toque.
- `DialogoConfirmar` no lanza nada al pulsar NO.

### 7.3 Checklist manual en el Redmi (antes de entregárselo)
- [ ] Se puede elegir como pantalla de inicio predeterminada, y ○ vuelve a ella desde cualquier app.
- [ ] Cada botón funciona, comprobado también con el Diagnóstico del modo administrador.
- [ ] La videollamada se abre directamente con cada familiar.
- [ ] El QR de DIA se lee en la caja real de la tienda.
- [ ] Un mensaje de texto, un audio y un mensaje de grupo enviados desde otro móvil aparecen en "Mensajes nuevos" y el aviso 🔴 sale en el familiar correcto. Al tocarlos se abre el chat correcto, y después de leerlos desaparecen.
- [ ] El botón "Abrir WhatsApp" abre la lista de chats.
- [ ] La temperatura aparece, y sin WiFi ni datos sigue mostrando el último valor.
- [ ] Tras reiniciar el móvil arranca en el launcher.
- [ ] Tras un día sin tocarlo, HyperOS no lo ha "matado".
- [ ] La abuela hace sola las 6 acciones del criterio de éxito, observada sin ayuda.

---

## 8. Riesgos y comprobaciones previas (fase 0)

| # | Riesgo | Cómo se comprueba | Si falla |
|---|---|---|---|
| R1 | La videollamada de WhatsApp vía `ContactsContract` no es una API oficial | Prototipo mínimo en el Redmi | Plan B: abrir el chat; ella pulsa el icono 📹 de WhatsApp |
| R2 | La app de ChatGPT no tiene un atajo de voz | Listar sus atajos con `LauncherApps` desde un prototipo | Abrir ChatGPT normal |
| R3 | El QR de la tarjeta DIA es dinámico | Comparar el QR de la app de DIA en dos días distintos, o probar uno generado en la caja | Modo "abrir la app de DIA" |
| R4 | HyperOS impide fijar launchers de terceros o rompe la navegación | Instalar un APK vacío con `CATEGORY_HOME` | Navegación con 3 botones. Si aun así falla, investigar los ajustes de HyperOS de esa versión |
| R5 | WhatsApp cambia el formato de sus notificaciones, o HyperOS desconecta el lector | Prototipo que vuelque a Logcat las notificaciones reales de WhatsApp (texto, audio, grupo) | Si el formato cambia: se muestra el texto tal cual. Siempre queda el botón "Abrir WhatsApp" |
| R6 | El administrador no sabe Android | — | Plan con fases pequeñas que se pueden probar en el móvil desde el día 1, y cada concepto explicado |

---

## 9. Fases de entrega (resumen; el detalle irá en el plan de implementación)
0. **Preparar el entorno y comprobar riesgos:** Android Studio, depuración USB en el Redmi, "hola mundo" como launcher, y prototipos de R1, R2, R3 y R5.
1. **Esqueleto del launcher:** `MainActivity` HOME, tema de alto contraste y pantalla de inicio con 5 botones que todavía no hacen nada.
2. **Acciones simples:** Cámara, YouTube y ChatGPT voz, más la pantalla de error, el antirrebote, la voz y la vibración.
3. **Familia:** datos y repositorio, pantalla Familia, chat de WhatsApp, videollamada y confirmación, y botón "Abrir WhatsApp".
3b. **Mensajes nuevos:** lector de notificaciones, sección "📩 Mensajes nuevos", avisos 🔴 en cada familiar y permiso en el asistente.
4. **Tarjeta DIA:** QR o código de barras con ZXing, brillo al máximo, o modo app.
5. **Reloj y temperatura:** Open-Meteo y caché.
6. **Modo administrador:** PIN, asistente inicial, edición de familiares y ajustes, y diagnóstico.
7. **Pulido y entrega:** checklist manual, guía de instalación para HyperOS y prueba con la abuela.
