# Asistente local experimental de ACCESSIA

Estado: implementación en desarrollo, sin publicación de una versión completa. No sustituye las descargas beta existentes ni satisface la puerta de pruebas físicas del PR #2.

## Uso y alcance

Android: menú ⋯ → Asistente local. Windows: botón Asistente local. El asistente está apagado al iniciar; la descarga del modelo requiere aceptación explícita. No se abre un servidor HTTP ni se necesitan Python, Docker o una cuenta de un proveedor de IA en el dispositivo. Se reutilizan motores abiertos existentes: LiteRT-LM 0.18.0 y LLamaSharp/CPU 0.30.0. Las pruebas de desarrollo pueden usar herramientas adicionales solo en el entorno aislado.

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

Evaluación inicial: Qwen3-0.6B produjo propuestas erróneas para búsquedas, entre ellas copiar un marcador o responder sobre la página en vez de buscar. Es una limitación funcional comprobada, aunque los filtros impidan ejecutar salidas inválidas. Se evalúa Qwen3-1.7B INT4 antes de fijar el modelo recomendado. No declarar aprobado el asistente por pasar solo sus validadores.

Los artefactos de las pruebas virtuales deben indicar SHA, plataforma y ausencia de validación física. Las imágenes de emulador/VM son capturas de preview, no imágenes de una versión estable. La galería histórica y los instaladores publicados permanecen separados.

## Pendientes de aceptación

Elegir un modelo que supere peticiones y paráfrasis en español con argumentos correctos; completar pruebas virtuales por sistema; medir en hardware autorizado cuando finalice la transferencia; revisar accesibilidad con usuarios, privacidad y distribución firmada/optimizada. Hasta entonces esta función sigue siendo experimental.

El teléfono del responsable, su memoria interna, Download/Descargas y la unidad USB-C permanecen fuera de toda prueba hasta su confirmación expresa de fin de transferencia.
