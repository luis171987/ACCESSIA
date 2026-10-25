# Agente local por voz — primera integración experimental

El responsable autorizó continuar y tomar decisiones técnicas el 24-10-2026, con trabajo limitado a entornos de desarrollo y pruebas aislados. Se elige IA local como base; no se añade nube ni credenciales. Esta entrega será experimental, no una versión completa ni una promesa de ejecución universal o de ausencia de errores.

Reutilizar LiteRT-LM (motor de AI Edge Gallery, Apache-2.0) en Android y LLamaSharp/llama.cpp (MIT) en Windows. No reemplazar GeckoView por Chromium ni copiar un navegador AGPL. Qwen3-0.6B cuantizado es el candidato inicial de distribución Apache-2.0; comprobar archivos, SHA256 y tamaño antes de habilitarlos. La aplicación sigue funcionando sin modelo. No descargar modelos al instalar ni al abrir sin consentimiento explícito y espacio suficiente.

La primera entrega interpreta una petición por turno y propone una acción o responde sobre la página observada. No implementa cadenas autónomas entre sitios: tras navegar se requiere una nueva petición. Ofrecer entrada por texto y micrófono, cancelación, confirmación por voz y botones. Conservar comandos existentes como alternativa.

## Contrato común

`Accessia.agentObserve()` devuelve `{ok,snapshot,url,title,text,controls:[{id,name,role,href}],sources:[{id,url,title}]}`. Solo controles visibles y habilitados, máximo 40; texto máximo 4500 caracteres. No incluir valores de campos. Instantánea opaca limitada al documento, máximo 120 segundos; acciones sobre controles cambiados o documentos distintos se rechazan.

`Accessia.agentExecute(proposal, confirmed=false)` admite únicamente `{action,snapshot,target?,value?,text?,citations?}`. Acciones: `read`, `scroll_up`, `scroll_down`, `focus`, `click`, `fill`, `search`, `open`, `answer`, `clarify`. Se ignoran propiedades inesperadas del modelo en los adaptadores; la confirmación es un parámetro separado establecido por el host, nunca por el modelo.

Lectura, foco y desplazamiento son inmediatos; navegación/búsqueda, clic y escritura requieren confirmación accesible en esta primera entrega conservadora. La confirmación es de un único pedido, se invalida al navegar/cancelar/detener la aplicación y vuelve a validar instantánea y control. Nunca rellenar contraseñas, pagos ni archivos. Bloquear acciones de IA en posibles evaluaciones. No ejecutar código o selectores generados.

`agentExecute` devuelve `{ok,message,text?,sources?,requiresConfirmation?,navigation?}`. `navigation` solo después de confirmación: `{url}` para búsqueda o apertura HTTPS. Los adaptadores nativos son los únicos que navegan. Un clic informa que fue enviado; nunca acredita el resultado externo. `answer` exige referencias a fuentes observadas; se muestran sus URLs reales y se advierte que el resumen puede contener errores. Respuestas sin evidencia se rechazan o solicitan aclaración.

El host inicia la solicitud exclusivamente por entrada del usuario. Android usa el puerto nativo aislado y un ID de solicitud con caducidad; Windows obtiene resultados de `ExecuteScriptAsync`, no de mensajes web que puedan iniciar acciones. La página, el texto extraído y los enlaces son datos no confiables. Límites: una inferencia simultánea, salida acotada, cancelación invalida la generación aunque el motor tarde en detenerse. No activar shell, archivos o control global del dispositivo.

## Instalación y límites

Incluir los motores como dependencias de cada aplicación y guardar los modelos en datos privados de ACCESSIA. Configuración visible del asistente con explicación de descarga, tamaño y borrado; verificación SHA256, archivo parcial y sustitución atómica. No usar almacenamiento compartido ni dispositivos externos para los modelos. Android conserva minSdk 28: si LiteRT requiere un API superior, usar un adaptador aislado y mostrar la incompatibilidad sin romper funciones existentes. El reconocimiento de voz del sistema puede usar red: no anunciar voz íntegramente offline.

La inferencia se ejecuta dentro de la aplicación; no abrir un servidor HTTP innecesario. Esto satisface ejecución local sin exponer puertos ni exigir Termux, Python, Docker, cuentas externas o un servicio privilegiado. La primera carga del modelo y su descarga pueden ser lentas; no prometer velocidad o memoria del Honor sin mediciones.

Decisión verificada durante implementación: usar llamadas estructuradas a herramientas con ejecución automática desactivada, no pedir JSON como texto libre. La ruta inicial falló la selección de acciones; las herramientas reales del runtime aprobaron las cuatro pruebas de humo en cada motor. Windows fija LLamaSharp/CPU 0.27.0 porque la prueba real de 0.30.0 detectó incompatibilidad con .NET8 pese a compilar. El navegador conserva la autoridad de validar y ejecutar una sola propuesta.

## Verificación y distribución

Pruebas de instantáneas obsoletas, campos privados, acciones desconocidas, confirmación, cancelación y citas. Pruebas nativas con modelo simulado para el flujo y compilación de ambos clientes. Cuando sea viable, probar inferencia real en este entorno, indicando que no representa un teléfono. Publicar rama/PR independiente del PR #2, sin fusionar ni cambiar las descargas beta actuales. Actualizar README con estado experimental y limitaciones; no fabricar capturas. Validación física y versión completa quedan pendientes hasta contar con autorización expresa y completar la validación física.
