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
