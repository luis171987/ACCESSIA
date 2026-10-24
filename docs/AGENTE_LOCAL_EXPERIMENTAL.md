# Asistente local experimental de ACCESSIA

Estado: implementación en desarrollo, sin publicación de una versión completa. No sustituye las descargas beta existentes ni satisface la puerta de pruebas físicas del PR #2.

## Uso y alcance

Android: menú ⋯ → Asistente local. Windows: botón Asistente local. El asistente está apagado al iniciar; la descarga del modelo requiere aceptación explícita. No se abre un servidor HTTP ni se necesitan Python, Docker o una cuenta de un proveedor de IA en el dispositivo. Se reutilizan motores abiertos existentes: LiteRT-LM 0.18.0 y LLamaSharp/CPU 0.27.0, compatible con .NET8. Las pruebas de desarrollo pueden usar herramientas adicionales solo en el entorno aislado.

Se puede escribir una petición o usar el reconocimiento de voz del sistema. Cada turno admite una acción sobre la página actual. Clic, escritura, búsqueda y apertura requieren confirmación por botón o voz; cancelar o navegar invalida el pedido. Solo se ejecutan herramientas permitidas: ningún código generado, shell o selector arbitrario. Las observaciones caducan y no incluyen valores de campos editables.

No implementa todavía secuencias autónomas entre sitios ni todas las funciones de un navegador. Se bloquean actuaciones de IA ante posibles evaluaciones. Un clic enviado no demuestra un pago, mensaje o formulario completado. Los resúmenes requieren una fuente observada y pueden contener errores; el sistema rechaza fuentes inventadas pero eso no prueba la veracidad de cada frase.

## Recursos y privacidad

- Android conserva el navegador para Android 9+; la función de IA se habilita de forma conservadora en Android 12+. Windows conserva .NET8/WebView2 y añade bibliotecas CPU de inferencia al paquete.
- El modelo se guarda en datos privados de la aplicación/cuenta, con archivo parcial, tamaño y SHA256 comprobados. La descarga puede consumir datos móviles; sin espacio suficiente se rechaza. No utiliza Download/Descargas ni USB-C.
- El APK debug de desarrollo contiene distintas arquitecturas y es grande (alrededor de 583 MiB en la primera compilación), aparte del modelo. No equivale al tamaño de una distribución optimizada por arquitectura.
- No hay proveedor de inferencia en la nube ni credenciales incorporadas. El reconocimiento de voz y la síntesis del sistema dependen de su configuración y pueden utilizar red. Navegar y buscar implica comunicación con los sitios consultados.
- Modelo, memoria, CPU, latencia y batería necesitan evaluación por dispositivo. No se ha medido el Honor Magic 7 Lite y no se afirman pruebas físicas.

## Pruebas y límites de evidencia

El flujo se comprueba con pruebas del motor compartido, políticas de las dos plataformas, controladores Android con motor simulado y compilaciones nativas. El workflow `Local agent virtual OS smoke tests` prepara Android API28/35/36 en emuladores x86_64 y arranque de la aplicación Windows en una VM. Su configuración no significa que las ejecuciones hayan aprobado: consultar el resultado y los artefactos de cada SHA.

Los smoke tests virtuales comprueban arranque e interfaz accesible sin descargar un modelo en el emulador. La inferencia real se evalúa aparte, en CPU Linux aislada, con solicitudes de prueba y sin acciones externas. Ninguna de esas pruebas sustituye micrófono, TalkBack, rendimiento o batería en un teléfono físico.

Evaluación inicial: pedir JSON como texto libre a Qwen3-0.6B produjo propuestas erróneas. Cambiar solo el texto de instrucciones o aumentar a 1.7B no resolvió ese conjunto. Se sustituyó por las herramientas nativas de LiteRT-LM con ejecución automática desactivada y por el formato de herramientas Qwen3 en Windows, con validación estricta. Ambos motores pasaron cuatro peticiones reales en CPU Linux: leer, buscar el tema solicitado, abrir un control observado y desplazar. El formato, los argumentos y el destino se comprobaron; no se ejecutaron acciones externas. [Evidencia GGUF](../windows/verification/2026-10-24-gguf-cpu.json). Este conjunto pequeño no mide precisión general y no permite prometer ausencia de errores.

Verificaciones locales del código: 42 pruebas Android, 25 JS, 24 Python, cuatro pruebas de DOM en Chromium real y 30 comprobaciones de políticas/integridad/procedencia Windows aprobadas; APK y paquete de pruebas compilados. La compilación Windows aprobó con un aviso existente de WebView2. Los resultados de Actions deben consultarse para el SHA exacto: una ejecución anterior de los emuladores falló al localizar la opción del menú; la prueba se corrigió para usar desplazamiento accesible y omitir la consulta opcional de IP en datos de prueba.

Los artefactos de las pruebas virtuales deben indicar SHA, plataforma y ausencia de validación física. Las imágenes de emulador/VM son capturas de preview, no imágenes de una versión estable. La galería histórica y los instaladores publicados permanecen separados.

Para reproducir la prueba LiteRT en un entorno de desarrollo aislado con Python y `litert-lm==0.18.0`, usar `python tools/test_agent_inference.py --model /ruta/Qwen3-0.6B.litertlm --report resultado.json`. El script comprueba el modelo fijado por tamaño/SHA256, no descarga archivos ni ejecuta acciones del navegador. [Resultados de las cuatro llamadas LiteRT](verificacion/2026-10-24-litert-cpu.json).

## Pendientes de aceptación

Elegir un modelo que supere peticiones y paráfrasis en español con argumentos correctos; completar pruebas virtuales por sistema; medir en hardware autorizado cuando finalice la transferencia; revisar accesibilidad con usuarios, privacidad y distribución firmada/optimizada. Hasta entonces esta función sigue siendo experimental.

El teléfono del responsable, su memoria interna, Download/Descargas y la unidad USB-C permanecen fuera de toda prueba hasta su confirmación expresa de fin de transferencia.
