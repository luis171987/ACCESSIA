# Revisión del PR #2 — 24 de octubre de 2026

ACCESSIA · Creado por GENAI INDUSTRIES · Luis Carlos Moreno Jiménez.

Se revisan los 19 hilos de https://github.com/luis171987/ACCESSIA/pull/2. Este documento separa correcciones verificables por software de pruebas que requieren un dispositivo físico.

## Alcance y comprobaciones

- Conflicto con `main`: README actualizado conservando descargas y advertencias actuales.
- Base anterior: 12 pruebas JavaScript y 24 pruebas Python aprobadas.
- Se incorporan pruebas nativas con JUnit y Robolectric. Simulan Android y sustituyen únicamente las fronteras externas (GeckoSession, motor de voz y TTS); no certifican funcionamiento real de los proveedores ni del navegador nativo.
- Los resultados finales y el commit estarán registrados en GitHub Actions y en las respuestas de cada hilo.
- Ciclo comprobado: las 17 pruebas nativas fallaron antes de las correcciones y aprobaron después (17/17, cero omitidas). También aprobaron 12/12 pruebas JavaScript, 24/24 Python y el escaneo de secretos (68 archivos de texto examinados, cero hallazgos en esa ejecución).

## Inventario de comentarios

| Comentario | Comportamiento esperado |
|---|---|
| 4342669812 | El botón flotante no intercepta el micrófono inferior. |
| 4342669818 | Las excepciones de inicio también cuentan para detenerse tras ocho errores. |
| 4342669828 | Los créditos solicitados por voz se anuncian sin bloquear en un diálogo. |
| 4342669836 | La descripción accesible del micrófono coincide con su acción. |
| 4342669850 | «Buscar en Google» conserva la consulta, mayúsculas y acentos. |
| 4342669863 | El panel flotante cabe y permite desplazamiento en ventanas cortas. |
| 4342669874 | La detención de voz anuncia su causa a accesibilidad. |
| 4342893583 | La lista de voces se muestra y permite seleccionar opciones. |
| 4342893590 | Los prefijos combinados se eliminan hasta estabilizarse. |
| 4342893594 | Ocultar el panel reinicia texto y descripción del botón. |
| 4343062714 | El micrófono de bienvenida refleja el mismo estado de voz. |
| 4343062720 | Cerrar página elimina su navegación Atrás/Adelante en vivo. |
| 4343062725 | El selector alternativo de Descargas abre el documento elegido. |
| 4343062728 | Enviar una dirección libera el foco para mostrar redirecciones. |
| 4343104422 | Abrir con prefijos corteses conserva puntos y guiones del dominio. |
| 4343104430 | Menú y recarga tienen descripciones semánticas para TalkBack. |
| 4343301839 | Enviar con el teclado finaliza el dictado igual que Ir. |
| 4343301844 | Aceptar idioma sugerido reconfigura también la respuesta TTS. |
| 4343301848 | Toda detención de voz limpia el modo de dictado acumulativo. |

## Puerta de fusión: prueba física aún requerida

La incidencia #1 y el PR #2 exigen validar la nueva APK en Honor Magic 7 Lite antes de fusionar. Una APK anterior fue instalada el 21 de octubre, con problemas reportados; esto no valida las correcciones actuales.

Prueba pendiente con el dispositivo:

1. Instalar la APK de la revisión actual, preservando los datos existentes. No desinstalar versiones anteriores sin copia de sus datos.
2. Con TalkBack, activar/desactivar ambos micrófonos y comprobar etiquetas y anuncios de errores.
3. En horizontal y con letra ampliada, abrir, desplazar, ocultar y volver a abrir el panel flotante.
4. Dictar varios fragmentos y terminar con Ir y con el teclado; detener, reactivar y comprobar comandos normales. Probar ir al fondo y denegar permiso.
5. Probar prefijos combinados, dominios con punto, créditos por voz y cambio de idioma/voz TTS.
6. Abrir dos páginas, cerrar la actual y confirmar que Atrás/Adelante no la recuperan.
7. Probar Descargas en un teléfono sin gestor compatible y abrir un documento desde el selector.
8. Registrar commit/APK, versión Android, motor STT/TTS y resultados sin publicar datos personales.

No se declara validación física, accesibilidad certificada, firma de producción ni funcionamiento universal a partir de las pruebas automatizadas.
