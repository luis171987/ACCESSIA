# ACCESSIA: evaluación de agente local por voz

Fecha: 2026-10-24. Estado: investigación y propuesta; **agente no implementado ni incluido en los instaladores**. La elección de arquitectura y del modelo sigue pendiente. Este documento no acredita pruebas de rendimiento ni pruebas físicas.

## Necesidad

Permitir que una persona con discapacidad exprese con sus propias palabras qué desea hacer en ACCESSIA: buscar información, abrir y leer páginas, localizar controles y actuar sobre ellos. El navegador debe explicar lo observado, aportar enlaces reales cuando use información de Internet y reconocer los límites de lo que puede comprobar.

Se solicita instalación integrada y ejecución local. La nube se estudia únicamente como alternativa opcional, no como dependencia aceptada por el usuario.

## Lo que existe hoy

Se examinó el código de la rama del [PR #2, commit 53504d3](https://github.com/luis171987/ACCESSIA/tree/53504d330f1e178d843b7457b7264862fbd958b5), que todavía no está fusionado en main. Esta evaluación documental parte de main `e3a39b50745b85266ecf0bb02434e8352d01ea84`; no afirma que main contenga todas las mejoras de esa rama.

- Android: `handleSpoken` en `MainActivity.kt` reconoce frases mediante reglas; `sendWebCommand` transmite comandos a una extensión de GeckoView.
- Windows: `HandleSpeech` y `Command` en `Program.cs` enlazan reconocimiento de voz del sistema con WebView2.
- `shared/accessia.js` interpreta comandos y actúa sobre controles visibles, lectura, desplazamiento y vídeo. Sus resultados actuales no proporcionan una comprobación general del efecto de cada acción.
- El puente Android acepta cadenas de comandos; todavía no ofrece una API de herramientas con observaciones estructuradas, identificadores de controles y condiciones posteriores verificables.
- Los roles de agentes en la documentación son una propuesta de arquitectura, no modelos de IA ya ejecutándose.
- Hay implementaciones Android y Windows. iOS, macOS y Linux necesitarían trabajo específico; no se ofrecen aquí instaladores para esos sistemas.

## Alternativas evaluadas

| Alternativa | Ventajas | Costes y límites |
| --- | --- | --- |
| IA local general pequeña y herramientas controladas | Interpreta lenguaje y puede redactar respuestas sin enviar la conversación a un proveedor de modelos | Descarga, RAM, batería y calidad en español deben medirse; no todos los dispositivos podrán usar el mismo modelo |
| Modelo local especializado en acciones | Puede consumir menos recursos para un conjunto acotado de herramientas | Requiere adaptación y evaluación para ACCESSIA; por sí solo no sustituye un asistente conversacional general |
| IA local con nube opcional | Permite derivar tareas difíciles a un modelo mayor | Requiere consentimiento, cuenta o credenciales, conexión y posible coste; cambia el tratamiento de datos |

Recomendación técnica pendiente de elección del usuario: comenzar con IA local, Android y Windows; evaluar antes de escoger el modelo. Mantener las funciones actuales como alternativa si el dispositivo no reúne recursos. No introducir un servidor remoto obligatorio ni un entorno de desarrollo adicional para el usuario.

## Agentes existentes: reutilizar antes de crear desde cero

Se revisaron repositorios originales, documentación y licencias, además de 15 resultados de búsqueda de Exa sobre agentes existentes. La evaluación anterior de motores y modelos incluyó otros 30 resultados. Son revisiones documentales, no instalaciones ni ensayos de funcionamiento.

| Proyecto existente | Licencia del repositorio | Qué permite reutilizar | Encaje y límite para ACCESSIA |
| --- | --- | --- | --- |
| [Google AI Edge Gallery / Mobile Actions](https://github.com/google-ai-edge/gallery) | Apache-2.0 | Ejemplo Android en Kotlin de ejecución local, gestión de modelos y herramientas basadas en FunctionGemma; publica guía para añadir acciones propias | Primera base móvil a evaluar. No es un agente de navegación terminado: necesita herramientas para GeckoView, adaptación al español y verificación. Gallery exige Android 12 o posterior, mientras ACCESSIA declara minSdk 28; no asumir que su código se puede copiar sin elevar requisitos o aislar la función |
| [Nanobrowser](https://github.com/nanobrowser/nanobrowser) | Apache-2.0 | Planificación, navegación y proveedores de modelos locales, incluido servidor compatible | Buena referencia web. El README actual dice que su capa de navegación usa `chrome.debugger` y Puppeteer; soporte oficial Chrome/Edge y Firefox no soportado. No instalar sin adaptar en GeckoView ni asumir soporte en WebView2 por estar basado en Chromium |
| [Browser Use](https://github.com/browser-use/browser-use) | MIT | Agente web existente, herramientas personalizables, modelo y navegador locales o remotos | Candidato para evaluación de escritorio y comparación funcional. La biblioteca Python requiere Python >=3.11 y un navegador compatible; no se documenta aquí una integración directa en el APK de ACCESSIA |
| [Stagehand](https://github.com/browserbase/stagehand) | MIT | API de observar, actuar y extraer datos estructurados | Alternativa para escritorio. Su ruta local requiere Chrome; no equivale a soporte del GeckoView Android ni prueba de integración con WebView2 |
| [BrowserOS](https://github.com/browseros-ai/BrowserOS) | AGPL-3.0 | Navegador Chromium con agente y opción de modelos locales | Referencia de producto; sus plataformas documentadas son Windows/macOS/Linux. Reemplazar ACCESSIA por este fork sería un cambio mayor. Incorporar código AGPL exige evaluar sus obligaciones; no conservar simplemente MIT como única licencia del conjunto derivado |
| [Mobilerun, antes DroidRun](https://github.com/droidrun/mobilerun) | MIT | Agente de automatización móvil con herramientas nativas y distintos proveedores, incluido Ollama | Útil como referencia o futura herramienta de pruebas. El inicio rápido requiere Python y ADB/depuración; no satisface por sí solo la instalación autónoma de un asistente dentro de ACCESSIA en el teléfono |

Recomendación: evaluar primero la reutilización selectiva de **Mobile Actions / AI Edge Gallery** para Android y contrastar **Nanobrowser / Browser Use** para la navegación web y escritorio. Esto evita entrenar un modelo fundacional o diseñar todo el ciclo de agente desde cero, pero sigue requiriendo adaptar herramientas, accesibilidad y comprobación de resultados. No hay evidencia de un único proyecto de esta lista que cumpla todos los requisitos de ACCESSIA sin modificaciones.

La guía de Google permite definir herramientas Kotlin y enlazarlas con acciones de la aplicación; el modelo de ejemplo se adapta a funciones concretas. La licencia Apache-2.0 de la aplicación no sustituye los términos propios de los pesos del modelo. Cualquier reutilización deberá fijar el commit de origen, conservar avisos y registrar modificaciones y dependencias. No copiar credenciales, telemetría, herramientas generales del sistema ni capacidades de eludir CAPTCHA de ejemplos de terceros.

## Motores y modelos candidatos

| Candidato | Evidencia del fabricante o proyecto | Decisión pendiente |
| --- | --- | --- |
| llama.cpp | Ejemplos oficiales de integración Android y servidor para escritorio; salida restringida por esquema y soporte de herramientas | Verificar compatibilidad del modelo/formato, binarios por arquitectura, licencia y tamaño de distribución |
| Qwen3.5 0.8B / 2B | Tarjetas oficiales con licencia Apache-2.0; modelos pequeños orientados también a prototipos y adaptación | Medir comprensión del español, selección de acciones, memoria y velocidad; el número de parámetros no garantiza idoneidad |
| FunctionGemma 270M | Modelo especializado en llamadas a funciones; Google indica adaptación a las herramientas propias y que no es un modelo de diálogo directo | Evaluar entrenamiento para acciones de ACCESSIA y español; revisar términos Gemma y motor móvil compatible |
| Gemma 3n | Diseñado para dispositivos; reducción de memoria mediante técnicas específicas | E2B no significa que una carga convencional almacene solamente 2B parámetros; medir la implementación real |

No se ha descargado ni ejecutado ninguno de estos modelos para ACCESSIA. La investigación no demuestra aún que llama.cpp ejecute todos los candidatos con el formato y aceleración deseados. No hay un presupuesto de RAM o latencia validado para el Honor del usuario.

Si «CLOUD» se refiere a Claude: la documentación de Anthropic sobre herramientas autoalojadas indica que la inferencia sigue ocurriendo en su infraestructura y los datos de herramientas pasan por su servicio. Esa opción no cumple por sí sola el requisito de inferencia íntegramente local.

## Condiciones que deberá resolver el diseño

1. **Comprensión y ejecución separadas.** El modelo propone una herramienta con argumentos; el navegador valida tipos, permisos, origen y contexto antes de ejecutarla. No ejecutar JavaScript, comandos del sistema ni selectores arbitrarios generados por el modelo.
2. **Contexto verificable.** Exponer al agente los controles visibles permitidos con identificadores temporales, URL y versión del documento. Rechazar acciones sobre una observación caducada tras cambiar de página o pestaña.
3. **Comprobación del resultado.** Distinguir petición comprendida, acción intentada y efecto observado. No anunciar una compra, envío o cambio como completado solo porque se despachó un clic. Usar límites de pasos y tiempo, cancelación accesible y detección de bloqueos.
4. **Fuentes reales.** Vincular las afirmaciones a páginas efectivamente consultadas, con sus enlaces. Validar que los identificadores de citas corresponden a esas fuentes. Si falta evidencia, reconocerlo o pedir aclaración; no prometer cero errores ni inventar enlaces.
5. **Permisos por impacto.** Pedir confirmación accesible, también por voz, para enviar mensajes, comprar, eliminar o realizar cambios sensibles. No exigir confirmación por cada desplazamiento o lectura. La página no puede conceder permisos al agente mediante instrucciones incrustadas.
6. **Límites existentes.** Conservar restricciones educativas y de seguridad: no resolver evaluaciones por cuenta de la persona, eludir CAPTCHA o DRM, ni ampliar automáticamente el acceso al resto del dispositivo.
7. **Voz y privacidad.** La inferencia local no hace local al reconocedor de voz actual. Comprobar STT y TTS sin conexión por sistema, ofrecer texto cuando no estén disponibles y describir claramente cualquier envío de audio o texto a terceros. Las búsquedas web seguirán necesitando Internet.
8. **Servicio local.** En Android preferir integración nativa o servicio ligado al uso de la aplicación. En Windows evaluar un proceso incluido y gestionado por ACCESSIA. Si se usa HTTP, escuchar solo en loopback, con autenticación de sesión y orígenes restringidos; no exponer archivos ni herramientas generales del sistema.
9. **Instalación y actualizaciones.** Preparar el componente al primer inicio visible, con permisos y comprobación de recursos. Android restringe el inicio de servicios y el micrófono desde segundo plano: no prometer un agente escuchando inmediatamente al terminar la instalación. Verificar procedencia, licencia, integridad y compatibilidad de modelos y binarios; permitir cancelar descargas y retirar modelos.
10. **Distribución accesible.** Cada versión completa deberá enlazar desde el README a sus instaladores reales y mostrar capturas auténticas de esa versión; conservar su archivo cronológico. No anunciar esta función en las descargas actuales ni sustituir capturas por ilustraciones.

## Evidencia necesaria antes de publicar una versión con agente

- Banco de peticiones en español, incluidas paráfrasis, habla incompleta y ambigüedad; evaluar con participación voluntaria de personas con distintas necesidades de accesibilidad.
- Pruebas de acciones sobre páginas controladas: buscar, leer, abrir, rellenar y cancelar, incluyendo páginas dinámicas y navegación interrumpida.
- Pruebas negativas: contenido malicioso en páginas, citas inventadas, acciones no autorizadas, controles ocultos, instrucciones obsoletas y ausencia de evidencia.
- Medición de almacenamiento, RAM máxima, latencia de voz a acción, batería y estabilidad por dispositivo; acordar umbrales de aceptación a partir de medidas, no de publicidad del modelo.
- Verificar instalación limpia, actualización, descarga interrumpida, falta de espacio, revocación del micrófono y modo sin conexión. Documentar separadamente pruebas automatizadas y físicas, con versión y dispositivo.

## Situación del teléfono y trabajo pendiente

El Honor Magic 7 Lite sigue en pausa mientras se trasladan archivos. No acceder a Download/Descargas ni al USB-C; no instalar, desinstalar, descargar modelos ni efectuar pruebas o capturas en ese teléfono hasta que el usuario confirme que terminó el traspaso.

Pendiente: elegir IA local o nube opcional; cerrar el diseño; comprobar licencias y compatibilidad del motor/modelo; implementar y validar en una rama propia. La decisión de fusionar el PR #2 y su validación física continúan siendo asuntos separados. Este documento no modifica código, instaladores, comentarios de revisión ni el estado de esa fusión.

## Fuentes primarias consultadas

- [llama.cpp: integración Android](https://github.com/ggml-org/llama.cpp/blob/HEAD/docs/android.md).
- [llama.cpp: servidor y configuración](https://github.com/ggml-org/llama.cpp/blob/HEAD/tools/server/README.md).
- [llama.cpp: llamadas a herramientas](https://github.com/ggml-org/llama.cpp/blob/master/docs/function-calling.md).
- [Qwen3.5 0.8B: tarjeta oficial](https://huggingface.co/Qwen/Qwen3.5-0.8B).
- [Qwen3.5 2B: tarjeta oficial](https://huggingface.co/Qwen/Qwen3.5-2B).
- [FunctionGemma: tarjeta oficial](https://ai.google.dev/gemma/docs/functiongemma/model_card).
- [FunctionGemma: acciones móviles](https://ai.google.dev/gemma/docs/mobile-actions).
- [AI Edge Gallery: guía para añadir funciones propias](https://github.com/google-ai-edge/gallery/blob/main/Function_Calling_Guide.md).
- [Gemma 3n: arquitectura y memoria](https://ai.google.dev/gemma/docs/gemma-3n).
- [Anthropic: herramientas en entornos autoalojados](https://platform.claude.com/docs/en/managed-agents/self-hosted-sandboxes).
- [Android: restricciones de servicios desde segundo plano y micrófono](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start).

Las fuentes pueden evolucionar después de la fecha de esta evaluación. Ninguna garantiza por sí misma compatibilidad, seguridad, accesibilidad o rendimiento de ACCESSIA.
