# Windows — ACCESSIA, creado por GENAI INDUSTRIES

Creador: Luis Carlos Moreno Jiménez, Santa Cruz de la Sierra, Bolivia.


Requiere Windows 10/11, Microsoft WebView2 Runtime y SDK .NET 8 para compilación.

```
cd windows/Accessia.Windows
dotnet restore
dotnet build -c Release
dotnet publish -c Release -r win-x64 --self-contained false
```

La creación de un instalador `.exe` firmado es una actividad **posterior a QA**. `dotnet publish` genera una aplicación para Windows, pero no un instalador asistido ni una distribución certificada.

El reconocimiento de voz depende de la instalación del motor de español de Windows (System.Speech). No hay motor garantizado para todos los equipos; sin él permanece disponible la interfaz de botones accesibles. Los comandos de 'estudio automático' en voz exigen confirmación verbal adicional. Esta versión no accede al teclado global ni controla otras apps.

La compilación cruzada se verificó en Linux y también se compila en runners Windows de GitHub Actions. El arranque de una ventana, la preparación de WebView2, la inferencia y el micrófono son comprobaciones distintas; consultar la evidencia de cada revisión. No se afirman pruebas físicas.

## Asistente local experimental

El botón **Asistente local** permite activar por sesión y consentir la descarga opcional. No está activado al abrir. El modelo se guarda exclusivamente en `%LOCALAPPDATA%/ACCESSIA/models`; se puede borrar desde la misma configuración. Necesita al menos unos 900 MB libres para descargar y memoria adicional durante inferencia CPU. No requiere Python, Node, servidores, cuenta ni claves. La voz de Windows depende del motor de español instalado y puede usar red.

Motor empaquetado: `LLamaSharp` y `LLamaSharp.Backend.Cpu` **0.27.0**, licencia MIT (llama.cpp también MIT). Modelo oficial **Qwen3-0.6B Q8_0**, Apache-2.0, repositorio `Qwen/Qwen3-0.6B-GGUF`, revisión `23749fefcc72300e3a2ad315e1317431b06b590a`, archivo `Qwen3-0.6B-Q8_0.gguf`, **639446688 bytes**, SHA256 `9465e63a22add5354d9bb4b99e90117043c7124007664907259bd16d043bb031`. Metadatos contrastados con la API pública de Hugging Face el 24-10-2026; descarga HTTPS fijada a esa revisión. La descarga parcial se verifica antes de reemplazo atómico y vuelve a verificarse antes de inferencia.

Entrada por texto o voz: una petición por turno. Clic, escritura, apertura y búsqueda requieren una confirmación nativa separada con destino visible. Cancelar, navegar o cerrar invalida resultados tardíos. Los resultados RPC se obtienen de `ExecuteScriptAsync`; ningún mensaje web inicia acciones del asistente. El host calcula navegación desde la propuesta confirmada y nunca desde una URL devuelta por la página. Respuestas y resultados de clic no acreditan efectos externos. El modelo pequeño puede fallar o producir JSON inválido: se informa el error sin ejecutar una acción alternativa.

Pruebas de políticas independientes: `dotnet run --project windows/Accessia.Agent.Tests`. Compilación desde Linux: `dotnet build windows/Accessia.Windows -p:EnableWindowsTargeting=true`. La compilación cruzada no prueba WebView2, micrófono, accesibilidad ni inferencia física en Windows.

La inferencia usa la plantilla nativa de herramientas de Qwen3 con las diez funciones de `shared/agent-tools.json`. Solo se acepta una llamada `<tool_call>` con argumentos de la función elegida; se transforma a una propuesta validada por el host. El contexto está limitado a 8192 tokens, salida a 256 y plazo a 90 segundos; una respuesta inválida o tardía no ejecuta acciones. Para una prueba de inferencia real en una máquina de desarrollo, `dotnet run --project windows/Accessia.Agent.Integration -- --download-consented` permite expresamente descargar el modelo documentado y ejecuta cuatro peticiones sin navegador. Esta prueba nunca se ejecuta al instalar o abrir ACCESSIA.
