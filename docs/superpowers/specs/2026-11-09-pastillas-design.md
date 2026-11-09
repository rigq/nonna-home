# Pastillas sin olvidos — Diseño

Fecha: 2026-11-09 · Proyecto: Inicio Nonna (`com.fabri.nonna`) · Estado: aprobado en conversación

## 1. Objetivo

Que la Nonna se tome las pastillas de la mañana y de la noche sin olvidos y **sin agobiarla**, y que Fabri
(y el resto de la familia, si quiere) se enteren solo cuando no las ha marcado.

## 2. Decisiones tomadas con Fabri

| Tema | Decisión |
|---|---|
| Horas | 08:50 (mañana) y 20:50 (noche), cambiables en Ajustes |
| Nombres en pantalla | "Pastillas de la mañana" / "Pastillas de la noche" |
| Aviso de la mañana | Sonido suave + voz ("Buenos días, Nonna. Toca la pastilla de la mañana.") + notificación |
| Aviso de la noche | Solo vibración + notificación, **sin voz** (puede estar caminando por la calle) |
| Repeticiones | Ninguna: un solo aviso por toma, sin alarma insistente |
| Marcar | Dos toques: franja en Inicio → "¿Te has tomado las pastillas…?" → SÍ, YA ME LAS TOMÉ / TODAVÍA NO |
| Pendiente hasta | Mañana: hasta las 20:50. Noche: hasta las 08:50 del día siguiente |
| Aviso a la familia | ntfy (app gratuita, sin cuenta). WhatsApp descartado: no permite envío automático |
| Espera antes de avisar | 1 h por la mañana (09:50), 2 h por la noche (22:50) |
| Su alarma con música actual | Se mantiene al principio; Fabri la quita cuando vea que esto funciona |

## 3. Experiencia de la Nonna

1. A la hora, llega una notificación ("💊 Pastillas de la mañana · Toca aquí cuando te las tomes").
   Por la mañana suena suave y la voz la anuncia; por la noche solo vibra.
2. Mientras haya una toma pendiente, la pantalla de inicio muestra arriba una **franja grande**:
   "💊 PASTILLAS DE LA MAÑANA — Toca aquí cuando te las tomes".
3. Al tocarla: pantalla con "¿Te has tomado las pastillas de la mañana?" y dos botones enormes:
   **SÍ, YA ME LAS TOMÉ** (verde) y **TODAVÍA NO** (gris). El antirrebote se reinicia al abrirla
   (mismo patrón que la pantalla de opciones de un familiar).
4. Al decir SÍ: voz "¡Muy bien, Nonna!", la franja desaparece y se quita la notificación.
5. Si vuelve tarde de caminar, la franja sigue esperándola hasta la siguiente toma.

## 4. Experiencia de la familia

- Si a las 09:50 (mañana) o 22:50 (noche) la toma no está marcada, llega a ntfy:
  "💊 La Nonna no ha marcado las pastillas de la mañana".
- El canal ntfy es un nombre secreto generado por la app (`nonna-` + 16 caracteres hex). Quien quiera
  recibir avisos instala ntfy y se suscribe a ese canal.
- Sin internet en ese momento: se reintenta hasta 3 veces, cada 10 minutos.

## 5. Configuración (admin → Ajustes → sección "Pastillas")

- Interruptor "Recordar las pastillas". Al activarlo se pide el permiso de notificaciones (Android 13+)
  y se guarda la fecha de activación (las tomas anteriores a esa fecha nunca cuentan como pendientes).
- Hora de la mañana y hora de la noche (texto `08:50`; acepta `8:50`, `8.50`, `08h50`). La de la
  mañana debe ser anterior a la de la noche; si no, Guardar queda desactivado con un aviso.
- Interruptor "Voz por la mañana".
- Canal de aviso: se muestra el canal, botones **Copiar**, **Nuevo canal** y **Probar aviso**, e
  instrucciones: "Instala ntfy → + → escribe este canal".
- Comprobar botones (si las pastillas están activas): notificaciones permitidas, alarmas exactas
  permitidas, canal de aviso configurado; y en "Probar acciones": simular aviso de la mañana y simular
  la comprobación (envía el aviso a ntfy si no está marcada).

## 6. Arquitectura

- **Lógica pura** (`pastillas/Pastillas.kt`, con tests): qué toma está pendiente, marcar una toma,
  calendario de eventos (RECORDAR / COMPROBAR por toma y día), siguiente evento, si hay que avisar,
  parseo/formato de horas, conversión a milisegundos con zona horaria.
- **Aviso remoto** (`pastillas/Ntfy.kt`, con tests): generación y validación del canal, construcción del
  aviso (JSON de publicación de ntfy: `topic`, `title`, `message`, `tags`) y envío por HTTP POST.
- **Persistencia**: campos nuevos en `Ajustes` y un registro `TomasHechas` (fecha ISO de la última toma
  marcada de mañana y de noche) en el mismo DataStore.
- **Alarmas**: una única alarma programada en cada momento (el siguiente evento) con
  `AlarmManager.setAlarmClock` (la más fiable frente al ahorro de batería de MIUI); si Android no
  permite alarmas exactas, `setAndAllowWhileIdle`. Se reprograma al dispararse, al arrancar el móvil,
  al actualizar la app, al cambiar la hora o la zona, al abrir la app y al guardar Ajustes.
- **Receptor** (`ReceptorPastillas`): RECORDAR → notificación (+ voz por la mañana); COMPROBAR → si no
  está marcada, aviso ntfy (con reintentos).
- **Descartado**: WorkManager (no garantiza la hora exacta).

## 7. Fuera de alcance

- Pastillero físico, nombres de medicamentos individuales, más de dos tomas al día, historial o
  estadísticas, aviso "ya se la tomó" a la familia.

## 8. Pruebas

- Unitarias: lógica de pendientes (incluido marcar la de la noche pasada la medianoche y el día de
  activación), calendario y siguiente evento (incluido cruzar la medianoche), aviso sí/no, parseo de horas,
  cambio de hora de verano, canal ntfy y JSON del aviso, compatibilidad de `Ajustes` guardados antiguos.
- En el Redmi: simular aviso y comprobación desde Comprobar botones, ver la franja y marcar, recibir el
  aviso de prueba en ntfy, reiniciar el móvil y comprobar que la alarma sigue programada
  (`adb shell dumpsys alarm`).
