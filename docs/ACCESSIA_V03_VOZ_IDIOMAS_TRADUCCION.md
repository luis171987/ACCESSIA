# ACCESSIA v0.3 — Diagnóstico experimental de voz, idioma y traducción

**Proyecto:** ACCESSIA | GENAI INDUSTRIES. **Autor y propietario único:** Luis Carlos Moreno Jiménez, Santa Cruz de la Sierra, Bolivia.  
**Estado:** rama de desarrollo; no equivale a validación funcional en el Honor Magic 7 Lite.

## Evidencia de campo: video 1001089973.mp4

Grabación aportada por el autor (~245 s; 580 × 1280): la APK navega por Google y Anthropic, pero en varias ocasiones indica «Capturando tu voz», «Procesando frase», «Comando no reconocido» o «No se comprendió la frase». La barra superior muestra resultados de búsqueda que no siempre corresponden con la pronunciación pretendida. Esto no demuestra un fallo físico del micrófono.

### Causas observadas en el código anterior

1. `SpeechRecognizer` devolvía segmentos cortos; una consulta se ejecutaba usando **solo la primera hipótesis final**.
2. Los resultados parciales se mostraban pero se descartaban; la sesión de escucha terminaba por decisión del proveedor.
3. El reconocimiento `es-BO` era fijo de forma predeterminada; no todas las implementaciones tienen modelos adecuados para esa variante.
4. El motor `TextToSpeech` instalado determinaba el timbre. No se puede fabricar una voz femenina neuronal cambiando únicamente `setPitch()`.
5. `GeckoView` no proporciona por sí solo traducción universal, así como tampoco garantiza que todos los sitios tengan controles accesibles.

## Mejoras implementadas en la rama de revisión, pendientes de pruebas físicas

- **Dictado por fragmentos:** botón `Dictar +` y entrada del menú. Cada segmento final se acumula **como texto editable** en la barra de dirección. El usuario puede revisarlo, corregirlo y pulsar `Ir` cuando termine. El audio no se persiste.
- **Feedback parcial:** muestra el último texto parcial, **sin asumir que sea final ni enviarlo como búsqueda**. En errores `NO_MATCH`, indica si existió texto parcial.
- **Silencio:** se solicitan tiempos orientativos al reconocedor; **Android puede ignorarlos**, por lo que el dictado por fragmentos es la estrategia de continuidad fiable dentro de esta versión.
- **Idiomas de entrada:** predeterminado según idioma de Android; selector de idiomas comunes y entrada BCP 47 para cualquier etiqueta válida. La disponibilidad real depende del motor STT.
- **Preferencia de sitios:** `GeckoRuntimeSettings.setLocales` permite solicitar a las páginas un idioma preferido (no las traduce obligatoriamente).
- **Traducción:** entrada del menú que solicita consentimiento para enviar la **URL** de la página abierta a Google Traductor. El servicio puede no funcionar con páginas con sesión iniciada, dinámicas o restringidas. No enviar páginas sensibles.
- **Sugerencia regional por IP:** diálogo de consentimiento al primer inicio; solo si se acepta se consulta por HTTPS `https://ipapi.co/country/`. El proveedor recibe la IP pública. ACCESSIA no persiste la IP ni la localización; propone un idioma según país y **requiere confirmación**. Con VPN o sin red, se conserva el idioma del dispositivo. No se usan GPS, IMEI, contactos ni permisos de ubicación.
- El historial de texto de dictado solo reside temporalmente en el campo de búsqueda; no se implementa memoria persistente de conversaciones.

### Estado de localización y alcance real

La **elección del idioma de voz y la preferencia de idioma web** son una primera fase. La interfaz de ACCESSIA todavía tiene textos nativos escritos directamente en español. Para ofrecer interfaces completas en decenas de idiomas se deberán extraer cadenas a `res/values/strings.xml` y crear recursos traducidos, pruebas de accesibilidad, soporte RTL y preferencias por aplicación con `LocaleManager` (Android 13+). Es **deuda pendiente**, no una función ya implementada.

La detección automática por IP no permite inferir con certeza el idioma personal del usuario. El idioma del dispositivo y la elección expresa prevalecerán sobre cualquier recomendación de país.

## Comparativa de tecnologías de voz

| Motor | Modalidad | Ventajas | Riesgos / requisitos |
|---|---|---|---|
| Android SpeechRecognizer | Integración nativa, según proveedor | Sin modelos añadidos en APK; disponible en muchos Android | Fin de segmento controlado por proveedor; parcial y selección de idioma no garantizados |
| [Vosk Android](https://alphacephei.com/vosk/android) | Offline | Puede mejorar privacidad con modelos pequeños | Calidad y vocabulario dependen del modelo; requiere empaquetado, medición y mantenimiento |
| [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) | STT/TTS/VAD, offline y streaming según modelo | ARM64, Android, voz por segmentos, amplia oferta de arquitecturas | Consumirá CPU/RAM/almacenamiento; verificar modelo y licencia antes de integrar |
| Whisper (implementaciones móviles) | STT multilingüe | Reconocimiento flexible de frases | Mayor coste de inferencia; modelos grandes poco adecuados para baja memoria |
| [Piper](https://github.com/OHF-Voice/piper1-gpl/blob/main/docs/VOICES.md) | TTS offline | Voces españolas México, Argentina y España | Naturalidad específica variable; licencias de motor y modelo deben revisarse |
| [Microsoft Azure Neural TTS](https://learn.microsoft.com/en-us/azure/ai-services/speech-service/language-support) | TTS en nube | `es-BO-SofiaNeural` y `es-MX-DaliaNeural` son voces femeninas; probar muestras | Internet, facturación, latencia, consentimiento e infraestructura segura; **no colocar claves en APK ni GitHub** |

**Recomendación técnica:** comparar Android STT con al menos un motor streaming offline específico y medirlo en el Honor Magic 7 Lite. Para voz, seleccionar una voz TTS femenina latina instalada como alternativa local y evaluar un servicio neuronal mediante backend seguro, con selección explícita y una opción accesible offline. **No afirmar naturalidad 100 % humana sin evaluación auditiva.**

## Métricas mínimas para aceptación

- 30–50 frases en español boliviano: nombres propios, comandos, frases cortas y consultas de 15–35 palabras. Registrar **WER**, errores de intención, abandonos y falsos positivos.
- Latencias P50/P95 desde inicio de habla hasta transcripción final; sesiones cortas frente a dictado acumulativo; pruebas con ruido y micrófono interno.
- Consumo pico de RAM, CPU, batería y datos; comparaciones solo bajo iguales condiciones y red. Objetivo: no degradar GeckoView.
- Prueba ciega de voces por personas hispanohablantes para preferencia, inteligibilidad y naturalidad; probar Sofía/Dalia solo si se dispone del proveedor y permisos.
- Traducción: 10–20 sitios en diferentes idiomas; verificar si la opción abre correctamente, declara el uso del servicio y no transmite páginas privadas sin consentimiento.
- Privacidad: rechazar consulta IP, cambiar idioma manual, comprobar ausencia de IP en preferencias, y desactivar red.
- Compatibilidad: Android 13/14/16, API 35 target, accesibilidad TalkBack, descarga de voces donde sea pertinente.

## Hoja de ruta no implementada

- Segmentación/VAD real, reconocimiento streaming con sherpa-onnx y selección de modelo por idioma; almacenamiento local opcional y cuotas por modelo.
- Motor de intenciones multilingüe con pruebas y confirmación de acciones destructivas; memoria contextual **efímera**, opt-in para persistencia y borrado.
- TTS neuronal femenina latinoamericana integrada mediante proveedor autorizado y backend seguro; fallback local.
- Internacionalización nativa de todos los textos y compatible RTL.
- Traducción integrada más allá del enlace a Google Traductor, con comprobación de privacidad y permisos.

Referencias: [Android RecognizerIntent](https://developer.android.com/reference/android/speech/RecognizerIntent), [Android idiomas por aplicación](https://developer.android.com/guide/topics/resources/app-languages), [GeckoView settings](https://github.com/mozilla/gecko-dev/blob/master/mobile/android/geckoview/src/main/java/org/mozilla/geckoview/GeckoRuntimeSettings.java).
