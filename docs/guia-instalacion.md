# Instalar "Inicio Nonna" en el Redmi

## Primera vez
1. Opciones de desarrollador: **Depuración USB** + **Instalar vía USB** + **Depuración USB (ajustes de seguridad)**.
2. Android Studio → Run ▶ con el móvil conectado (o `adb install -r app/build/outputs/apk/debug/app-debug.apk`).
3. Ajustes → Pantalla de inicio → Navegación del sistema → **Botones**.
4. Ajustes → Apps → Apps predeterminadas → **Pantalla de inicio → Inicio Nonna**.
5. Ajustes → Apps → Administrar apps → Inicio Nonna:
   - **Inicio automático: activado**
   - **Ahorro de batería: Sin restricciones**
6. Seguridad → limpieza/aumentar velocidad: añade Inicio Nonna a las excepciones.
7. Completa el asistente: PIN, contactos, notificaciones, familiares.
8. Modo administrador (5 toques rápidos en el reloj + PIN) → Ajustes: ciudad, tarjeta DIA, nombre de ayuda.
9. Modo administrador → Comprobar botones: todo en ✅.
10. Cada familiar tiene que estar guardado en **Contactos** con el mismo número y tener WhatsApp.

## Actualizar
Conectar por USB → Run ▶. Los datos (familiares, ajustes) se conservan.

## Si algo va mal
- **Volver al launcher de Xiaomi:** modo administrador → "Salir al launcher normal".
- **Olvidé el PIN:** desinstalar y reinstalar (se pierde la configuración).
- **No salen los mensajes nuevos:** Comprobar botones → "Lector de mensajes conectado". Repetir el paso 5.
