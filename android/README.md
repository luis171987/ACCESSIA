# Android — ACCESSIA, creado por GENAI INDUSTRIES

Creador: Luis Carlos Moreno Jiménez, Santa Cruz de la Sierra, Bolivia.


Compilar y ejecutar en un entorno de desarrollo controlado. Se verificaron en cloud las pruebas nativas y `assembleDebug`/`assembleDebugAndroidTest`; no equivale a validación física.

- Requiere Android 9+ (minSdk28), prueba prevista en Android 16.
- JDK 17; AGP 9.4.0; Gradle 9.6.0; compileSdk 37.2.
- Dependencia nightly de GeckoView fijada provisionalmente; cambiar a canal estable tras la auditoría de API.
- Arranque con bienvenida local vacía, sin forzar Wikipedia ni una descarga. El botón «Estudio automático» requiere confirmación; no se persiste entre documentos.
- El motor `SpeechRecognizer` puede enviar audio al servicio de voz del SO. No asegurar procesamiento local; para un modo 100% offline, integrar Sherpa-ONNX en siguiente fase.
- **Limitación conocida:** `SpeechRecognizer` no garantiza escucha continua y Android restringe micro en segundo plano; la sesión se detiene cuando la app sale del primer plano.
- Se necesita validación con lector de pantalla TalkBack y simulación motora real. La base usa botones accesibles en lugar de gestos obligatorios.

## Asistente local experimental

En el menú **Asistente local** se puede consentir la descarga opcional de Qwen3-0.6B (613.722.264 bytes, Apache-2.0), activar el asistente y escribir una petición. Nunca descarga automáticamente. Se almacena en `filesDir/local-agent`, verifica tamaño y SHA256 y permite borrarlo. No utiliza almacenamiento compartido. El asistente está apagado al iniciar; sin modelo se conservan los controles y comandos habituales.

El adaptador usa LiteRT-LM 0.18.0 CPU, contexto total de 4096 tokens, salida máxima de 256 tokens y herramientas con ejecución automática desactivada; requiere Android 12 o posterior para habilitarse de forma conservadora, mientras que el navegador mantiene minSdk 28. El reconocimiento de voz de Android puede usar red. Con el asistente activo, voz y texto se interpretan localmente; «confirmar» autoriza únicamente la propuesta pendiente y «cancelar» la invalida. Clic, escritura, apertura y búsqueda muestran confirmación nativa. Navegar o poner la app en segundo plano invalida peticiones y confirmaciones. La cancelación descarta resultados tardíos; no garantiza interrumpir de inmediato el cálculo nativo. Desde la bienvenida puede proponer una búsqueda sin abrir primero una página.

La integración y las políticas se comprueban con un motor simulado. La inferencia real se probó separadamente en CPU Linux ([cuatro peticiones](../docs/verificacion/2026-10-24-litert-cpu.json)); no se ha probado inferencia, rendimiento o memoria en un teléfono. Las pruebas de interfaz usan emuladores aislados. No se ha realizado validación física.

Verificación local en cloud (24-10-2026): 46 pruebas nativas con Robolectric, APK debug y paquete de pruebas instrumentadas compilados. El APK universal de desarrollo ocupa aproximadamente 583 MiB, además del modelo opcional; no representa el tamaño final de distribución. Consultar los resultados de Actions por SHA para la validación virtual de cada revisión.
