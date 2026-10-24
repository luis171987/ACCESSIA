# Revisión del PR #2 — 24 de octubre de 2026

ACCESSIA · Creado por GENAI INDUSTRIES · Luis Carlos Moreno Jiménez.

Se revisan los 19 hilos originales y tres observaciones adicionales de https://github.com/luis171987/ACCESSIA/pull/2. Este documento separa correcciones verificables por software de pruebas que requieren un dispositivo físico.

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
| 4365738963 | La ayuda solicitada por voz anuncia comandos sin abrir un diálogo silencioso. |
| 4365738968 | Un dictado nuevo excluye la URL mostrada y conserva fragmentos escritos deliberadamente. |
| 4365751741 | El dictado conserva la primera hipótesis del reconocedor, en lugar de elegir la alternativa más larga. |

## Revisión independiente

Una revisión de código independiente encontró tres casos adicionales: callbacks del reconocedor anterior después de reactivar voz, descripción TalkBack desactualizada al reiniciar y etiquetas recortadas con fuente al 200 %. Se añadieron cuatro pruebas que fallaron antes de corregirlos: resultados antiguos tras reinicio, liberación del reconocedor detenido, estado accesible al volver a escuchar y ajuste de etiquetas con fuente ampliada.

La detención ahora invalida la generación del reconocedor y destruye su instancia; reactivar crea una nueva frontera de callbacks. Cada actualización de estado sincroniza texto y descripción accesible. Las acciones flotantes crecen con su contenido y el panel limita su altura disponible con desplazamiento.

## Segunda revisión automática

La revisión automática del commit `0221857` añadió dos observaciones. Se reprodujeron con pruebas: la ayuda por voz abría un diálogo silencioso y un dictado nuevo incorporaba la dirección de la página actual. La ayuda por voz ahora lee una guía breve sin diálogo; la guía visual se conserva en los botones. El inicio del dictado limpia únicamente el campo que coincide con la URL mostrada y conserva las consultas editadas. Las pruebas incluyen la búsqueda final resultante, la preservación de fragmentos y la emisión real de la llamada TTS de ayuda.

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

## Deuda de producto fuera de los comentarios revisados

La documentación previa conserva pendientes de mayor alcance: internacionalización completa de la interfaz, memoria contextual segura, STT/VAD streaming, selección de proveedor para voz neuronal y descargas delegadas desde GeckoView. Esta corrección del PR no implementa ni declara resueltos esos proyectos. Ver `ACCESSIA_V03_VOZ_IDIOMAS_TRADUCCION.md` e incidencia #1.

También se comprobó y corrigió la regresión de prefijos con puntuación, por ejemplo «ACCESSIA: por favor, abrir wikipedia.org», conservando el dominio original. Cada caso de apertura reinicia la URL de prueba para que una navegación anterior no oculte fallos.

## Resultado local final

- `gradle :app:testDebugUnitTest :app:assembleDebug`: BUILD SUCCESSFUL; 25 pruebas nativas, cero fallos, errores u omisiones. APK debug experimental compilada; no equivale a distribución firmada de producción.
- `node --test shared/tests/*.test.js`: 12 aprobadas.
- `python3 -m unittest discover -s tools/tests -p 'test_*.py'`: 24 aprobadas.
- `python3 tools/check_public_secrets.py`: 71 archivos de texto examinados; cero hallazgos en el árbol de trabajo. No es una auditoría completa del historial.
- Revisión independiente: tres observaciones corregidas y cubiertas con cuatro pruebas adicionales; sin observaciones menores aplazadas de esa revisión.
- Las advertencias de compilación sobre APIs Android obsoletas y bibliotecas nativas no depuradas siguen siendo advertencias, sin impedir la compilación; no se certifica ausencia de toda deuda técnica.

- Última verificación tras los dos comentarios automáticos adicionales: 24/24 pruebas nativas y APK compilada; se mantienen aprobadas las 12 pruebas JS y 24 Python y el escaneo de secretos.

La siguiente revisión automática señaló el orden de las hipótesis de reconocimiento: se elimina la elección por longitud y se conserva la primera alternativa no vacía del motor. La prueba de regresión falló antes de la corrección y ahora la verificación final aprueba 25/25 pruebas nativas, 12/12 JS y 24/24 Python, con APK compilada y escaneo sin hallazgos. El inventario incluye los 22 comentarios atendidos.
