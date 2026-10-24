# Android — ACCESSIA, creado por GENAI INDUSTRIES

Creador: Luis Carlos Moreno Jiménez, Santa Cruz de la Sierra, Bolivia.


Compilar y ejecutar únicamente en entorno controlado. No se ha validado `assembleDebug` aquí porque no se dispone del SDK/Gradle de Android ni de acceso a Maven.

- Requiere Android 9+ (minSdk28), prueba prevista en Android 16.
- JDK 17; AGP 9.0.1; Gradle 9.1.0; compileSdk 36.
- Dependencia nightly de GeckoView fijada provisionalmente; cambiar a canal estable tras la auditoría de API.
- Arranque con Wikipedia. El botón «Estudio automático» requiere confirmación; no se persiste entre documentos.
- El motor `SpeechRecognizer` puede enviar audio al servicio de voz del SO. No asegurar procesamiento local; para un modo 100% offline, integrar Sherpa-ONNX en siguiente fase.
- **Limitación conocida:** `SpeechRecognizer` no garantiza escucha continua y Android restringe micro en segundo plano; la sesión se detiene cuando la app sale del primer plano.
- Se necesita validación con lector de pantalla TalkBack y simulación motora real. La base usa botones accesibles en lugar de gestos obligatorios.

## Asistente local experimental

En el menú **Asistente local** se puede consentir la descarga opcional de Qwen3-0.6B (613.722.264 bytes, Apache-2.0), activar el asistente y escribir una petición. Nunca descarga automáticamente. Se almacena en `filesDir/local-agent`, verifica tamaño y SHA256 y permite borrarlo. No utiliza almacenamiento compartido. El asistente está apagado al iniciar; sin modelo se conservan los controles y comandos habituales.

El adaptador usa LiteRT-LM 0.18.0 CPU, contexto total de 4096 tokens, salida máxima de 512 tokens y una petición por turno; requiere Android 12 o posterior para habilitarse de forma conservadora, mientras que el navegador mantiene minSdk 28. El reconocimiento de voz de Android puede usar red. Con el asistente activo, voz y texto se interpretan localmente; «confirmar» autoriza únicamente la propuesta pendiente y «cancelar» la invalida. Clic, escritura, apertura y búsqueda muestran confirmación nativa. Navegar o poner la app en segundo plano invalida peticiones y confirmaciones. La cancelación descarta resultados tardíos; no garantiza interrumpir de inmediato el cálculo nativo.

La integración y las políticas se comprueban en el entorno de compilación con un motor simulado. No se ha cargado el modelo real ni probado inferencia, rendimiento o memoria en un teléfono. No se ha accedido a dispositivos, USB o sus archivos para desarrollar esta integración.

Verificación en cloud (24-10-2026): 36 pruebas nativas con Robolectric y APK debug compilada. El APK universal de desarrollo ocupa aproximadamente 583 MiB, además del modelo opcional; no representa el tamaño final de distribución.
