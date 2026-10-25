# ACCESSIA — Guía de comandos de voz y funciones

**Proyecto gratuito de accesibilidad, creado por GENAI INDUSTRIES.**  
Autor: Luis Carlos Moreno Jiménez, Santa Cruz de la Sierra, Bolivia.

> **Compatibilidad:** instrucciones para la versión beta Android en desarrollo. Algunas funciones dependen del motor de voz del dispositivo, de la conectividad y de la página abierta. Esta guía documenta funciones implementadas; no es un asistente conversacional universal ni una garantía de control sobre cualquier sitio.

## Cómo abrir esta guía desde ACCESSIA

- Pulsa el botón **Ayuda** en la cabecera.
- En el menú **⋯**, selecciona **Guía de comandos**.
- Con el micrófono activo, di **«ayuda»**, **«mostrar comandos»**, **«guía de comandos»** o **«qué puedo decir»**.
- La guía visual funciona **sin Internet y sin cargar una página web**. Selecciona una categoría para ver ejemplos y vuelve a «Más categorías» cuando quieras.

La ayuda solicitada por voz lee una guía breve de comandos sin abrir una ventana que requiera tocar la pantalla; el audio depende del motor TTS disponible. Los botones de Ayuda y del menú conservan la guía visual por categorías. Al iniciar **Dictar +** después de navegar, la dirección de la página mostrada se retira de la consulta nueva; los fragmentos escritos deliberadamente se conservan.

## Preparar el reconocimiento de voz

1. Abre ACCESSIA, pulsa **🎙 Activar voz** y concede el permiso de micrófono de Android.
2. Cuando aparezca «Escuchando», pronuncia una orden completa, en español natural, a volumen conversacional; **no hace falta gritar**.
3. Puedes cambiar el **Idioma para escuchar** desde **⋯**: español de Bolivia, general (España), México o Estados Unidos. Prueba distintas opciones si el motor del teléfono confunde el acento.
4. Para escuchar otra voz de respuesta, abre **⋯ → Elegir voz de respuesta**, selecciona una voz española instalada y escucha su demostración. Elige la femenina que te resulte más natural.
5. **Reconocer la voz** (STT) y **hablar una respuesta** (TTS) son funciones distintas. Seleccionar una voz femenina no cambia el reconocimiento de las palabras.

Android no identifica de manera uniforme el género de las voces TTS. Si el dispositivo no tiene una voz femenina natural, instala voces de mayor calidad desde los ajustes del motor **Texto a voz** del sistema, con información sobre redes y privacidad del proveedor.

## Catálogo de órdenes

| Función | Ejemplos de voz | Requisitos / comportamiento |
|---|---|---|
| Escuchar la guía | «ayuda», «mostrar comandos», «guía de comandos», «qué puedo decir» | Guía hablada sin diálogo; requiere TTS disponible y funciona también sin página abierta |
| Buscar información | «buscar universidades de Santa Cruz de la Sierra», «buscar en Google inclusión educativa» | Abre resultados de búsqueda; admite frases largas |
| Abrir un sitio | «abrir wikipedia.org», «abrir ejemplo.com» | Navega o busca el destino pronunciado |
| Regresar | «atrás», «volver», «página anterior» | Debe existir una página anterior |
| Avanzar | «adelante», «página siguiente» | Debe existir navegación hacia adelante |
| Actualizar | «recargar», «actualizar página» | Recarga la página actual |
| Cerrar página | «cerrar página» | Muestra la portada sin cerrar la aplicación |
| Salir | «salir», «cerrar navegador», «cerrar ACCESSIA» | Cierra la tarea de la aplicación |
| Favoritos | «guardar en favoritos», «ver favoritos» | Guardar exige una página HTTPS abierta |
| Historial | «ver historial», «mostrar historial» | Lista local de sitios HTTPS visitados; desde el cuadro se puede borrar con confirmación |
| Descargas | «descargas», «abrir descargas», «ver descargas» | Abre el gestor de descargas de Android; no equivale a descargar automáticamente archivos en GeckoView |
| Mostrar botones | «mostrar botones», «mostrar controles flotantes» | Enseña el panel de controles opcionales |
| Ocultar botones | «ocultar botones», «ocultar controles flotantes» | Esconde el panel; el botón flotante puede desactivarse desde ⋯ |
| Cambiar voz hablada | «elegir voz», «voz femenina», «probar voces» | Selector de voces españolas disponibles con prueba de escucha |
| Idioma de escucha | «idioma de voz», «cambiar idioma» | Cambia el idioma usado por el reconocedor |
| Detener micrófono | «desactivar voz», «apagar micrófono» | Apaga el reconocimiento de esta sesión |
| Información | «acerca de», «créditos» | Datos del proyecto y su autor |

### Controles accesibles dentro de una página

Estas órdenes actúan sobre la **extensión de accesibilidad**, no sobre el propio navegador. Necesitan que haya una **página abierta compatible** y que el enlace con la extensión funcione.

| Orden | Acción |
|---|---|
| «cursor siguiente» | Lleva el foco al siguiente control accesible |
| «cursor anterior» | Lleva el foco al control anterior |
| «seleccionar» | Activa voluntariamente el control enfocado |
| «pausar», «pausar vídeo» | Intenta pausar el vídeo HTML5 disponible |
| «continuar», «continuar vídeo» | Intenta reanudar el vídeo |
| «activar estudio automático» | Solicita confirmación antes de habilitar la continuidad educativa limitada |

**Importante:** La continuidad educativa no contesta evaluaciones, no elude CAPTCHA/DRM y no debe saltarse las decisiones del estudiante. La orden «activar estudio automático» exige «confirmar» o «cancelar» antes de actuar.

## ¿Por qué dice «Aún no hay controles accesibles en esta página»?

- Puede ocurrir cuando estás en la portada `about:blank`, sin ninguna web cargada.
- Los comandos del navegador **no deben depender de esa extensión**: por ejemplo, «ayuda», «salir» y «abrir descargas» son funciones locales.
- Las órdenes «cursor siguiente», «seleccionar» y «pausar vídeo» sí dependen de la página y del puerto de comunicación de la extensión.
- Para comenzar, escribe una búsqueda en la barra o di «buscar bibliotecas de Santa Cruz».

## Si reconoce mal una frase larga

- Espera a ver «Escuchando» antes de empezar; evita dictar durante la voz de respuesta.
- Habla **a velocidad normal, sin gritar**; si la frase es extensa, añade una pequeña pausa natural.
- Verifica permisos y que el micrófono no esté siendo utilizado por otra aplicación.
- Prueba es-BO o es-MX y comprueba la calidad de conexión si tu servicio STT utiliza Internet.
- El volumen del micrófono, el modelo acústico y la precisión del motor dependen principalmente de Android y del proveedor de reconocimiento. ACCESSIA **no dispone de una calibración mágica de ganancia ni garantiza exactitud perfecta**.
- Cuando la frase se interpreta como comando desconocido, di «ayuda» para consultar la sintaxis.

## Qué no está disponible todavía

- Control conversacional general sobre cualquier sitio web ni comprensión semántica ilimitada.
- Garantía de una voz femenina neuronal sin tener instalado un motor y paquete TTS compatibles.
- Descarga universal de enlaces y archivos iniciada desde GeckoView.
- Reconocimiento siempre activo con la aplicación en segundo plano.
- Certificación de accesibilidad para todas las páginas y dispositivos.

## Privacidad, seguridad y soporte

El reconocimiento se activa de manera voluntaria, utiliza el motor de Android configurado en el dispositivo y se detiene al salir o enviar ACCESSIA a segundo plano. Algunos servicios de reconocimiento y voces requieren conexión de red y pueden procesar datos según sus condiciones particulares; consulta la política del proveedor.

La instalación de una beta nueva firmada con otra clave debug puede fallar. Antes de desinstalar una beta, recuerda que **podrías perder favoritos e historial local**. Registra incidencias sin adjuntar contraseñas, cookies o información privada.

Repositorio: [luis171987/ACCESSIA](https://github.com/luis171987/ACCESSIA).  
Licencia del código: MIT. Identidad visual de ACCESSIA y GENAI INDUSTRIES sujeta a sus derechos correspondientes.
