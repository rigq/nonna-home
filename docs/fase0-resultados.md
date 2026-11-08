# Resultados fase 0 (fecha: 2026-11-08)

- Móvil: Redmi 220333QNY, Android 13
- R4 launcher predeterminado: PENDIENTE (la instalación por USB falló con INSTALL_FAILED_USER_RESTRICTED; hay que activar "Instalar vía USB")
- R2 atajo voz ChatGPT: id='voice_static_shortcut' corto='Búsqueda por voz' → palabras ["voice", "voz"] lo encuentran por el id. Falta confirmar en el móvil que abre el modo conversación.
- R1 videollamada WhatsApp: filas vnd.com.whatsapp.video.call presentes (97). Formato data1: '<número con prefijo>@s.whatsapp.net'. Falta probar el lanzamiento real.
- (d) paquete DIA: es.dia
- R3 tarjeta DIA: PENDIENTE (prueba manual de escanear el código dos veces)
- R5 notificaciones: PENDIENTE (tarea 12)

## Hallazgos en la prueba real (2026-11-08)

- **Videollamada directa:** WhatsApp (`CallContactLandingActivity`) exige `CALL_PHONE` al que lanza el intent
  de la fila `vnd.com.whatsapp.video.call`. Sin él: `SecurityException` y caída al chat. Con el permiso, llama
  directamente. ✅ Confirmado en el Redmi.
- **ChatGPT en voz:** el atajo de `LauncherApps` solo funciona siendo pantalla de inicio. El enlace
  `https://chatgpt.com/voice` (paquete `com.openai.chatgpt`) abre el modo voz siempre. ✅ Confirmado.
