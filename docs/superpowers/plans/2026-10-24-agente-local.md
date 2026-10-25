# Agente local — plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Integrar un primer asistente local experimental por voz y texto sin acceder al teléfono.
**Architecture:** LiteRT-LM Android y LLamaSharp Windows proponen acciones estructuradas. El motor común observa y valida las acciones; el host gestiona consentimiento, generación y navegación.
**Tech Stack:** Kotlin/JDK17, LiteRT-LM, JavaScript sin dependencias, C#/.NET8, LLamaSharp CPU.
**Spec:** `docs/superpowers/specs/2026-10-24-agente-local-design.md`.

## Global Constraints

- Realizar las pruebas en entornos aislados; las pruebas físicas requieren autorización expresa previa.
- No nube, credenciales, shell ni código generado; no puertos HTTP innecesarios.
- Mantener Android minSdk28 y controles existentes; informar incompatibilidad del motor si aplica.
- Modelo opcional descargado con consentimiento, tamaño y SHA256 comprobados en almacenamiento privado.
- Una petición/acción por turno, confirmación separada del modelo, resultados honestos y fuentes reales.
- Rama separada; no fusionar PR #2 ni anunciar versión estable o prueba física.

## Review Focus

- Respuestas tardías tras navegar, cancelar o cambiar de actividad no ejecutan acciones.
- Datos maliciosos de páginas/modelos no amplían las acciones ni conceden confirmaciones.
- Poco espacio, descarga incompleta o hash incorrecto no habilitan modelos.
- Controles modificados y citas inventadas se rechazan.
- Sin motor/modelo/permiso de voz se conservan botones y entrada por texto.

### Task 1: herramientas comunes y contrato de inferencia

Files: `shared/accessia.js`, `shared/agent-prompt.txt`, `shared/tests/agent.test.js`.
Consumes: DOM de la página; propuesta JSON validada por host.
Produces: `agentObserve()`, `agentExecute(proposal, confirmed=false)` según spec y prompt compartido.
- [ ] Escribir pruebas RED para controles privados/ocultos, confirmación, instantánea vieja y citas.
- [ ] Implementar herramientas; ejecutar `node --test shared/tests/*.test.js` hasta GREEN.
- [ ] Registrar resultado y commit.

### Task 2: Android

Files: clases nuevas `android/app/src/main/java/org/accessia/browser/agent/`, `MainActivity.kt`, extensión `bridge.js`, dependencias Gradle y pruebas nativas.
Consumes: contrato Task1, prompt compartido en assets, propuesta JSON de una inferencia.
Produces: motor local integrado, gestión de modelo privado, interfaz texto/voz con confirmación y cancelación.
- [ ] Pruebas RED de descarga/verificación/normalización y respuestas tardías con motor simulado.
- [ ] Integrar LiteRT-LM CPU sin iniciar modelo o descarga antes de consentimiento; límites y cierre en segundo plano.
- [ ] Enlazar puente con IDs y tiempo límite; usar comandos actuales como alternativa con asistente apagado.
- [ ] Ejecutar `gradle :app:testDebugUnitTest :app:assembleDebug` y registrar resultado.

### Task 3: Windows

Files: clases `windows/Accessia.Windows/Agent/`, `Program.cs`, csproj y pruebas de políticas independientes.
Consumes: contrato Task1 y prompt como asset.
Produces: inferencia LLamaSharp CPU integrada, descarga privada verificada, interfaz texto/voz y consentimiento.
- [ ] Pruebas RED de validación de modelo/propuesta y cancelación que puedan ejecutarse sin WinForms.
- [ ] Integrar runtime existente con contexto/salida limitados; no usar mensajes web para iniciar acciones.
- [ ] Ejecutar compilación .NET8 y pruebas; registrar límites de ejecución en Linux.

### Task 4: integración, revisión y documentación

Files: README principal, documentación del asistente, avisos de terceros y CI solo si necesario para pruebas nuevas.
- [ ] Ejecutar suites JS, Python, Android y compilación Windows; revisar todos los resultados.
- [ ] Revisar contrato común y las confirmaciones en ambos adaptadores con un revisor independiente.
- [ ] Corregir hallazgos importantes con pruebas; actualizar README con evidencias, sin sustituir instaladores anteriores.
- [ ] Publicar PR propio con resultados verificables y pendientes físicos explícitos.
