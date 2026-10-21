# ACCESSIA MVP Implementation Plan

**Autoría oficial:** GENAI INDUSTRIES — Luis Carlos Moreno Jiménez — Santa Cruz de la Sierra, Bolivia.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prototipo nativo para Android/Windows que controle videos y enlaces accesibles mediante instrucciones del usuario, con continuidad educativa optativa.

**Architecture:** Motor DOM JavaScript independiente, integrado en GeckoView mediante WebExtension y en WebView2 mediante inyección de código administrada. APIs de voz nativas por plataforma.

**Tech Stack:** JavaScript (Node test runner), Kotlin/GeckoView/Android SDK 36, C#/.NET 8/WinForms/WebView2/System.Speech.

**Spec:** `docs/superpowers/specs/2026-10-21-accessia-design.md`

## Global Constraints
- Público, sin fines de lucro en su distribución principal, licencia MIT.
- Sin bypass de CAPTCHA, DRM, autenticación, exámenes, políticas antibot.
- Usuario conserva el control; modo automático solo bajo autorización por sitio/sesión.
- Accesibilidad en español primero; código claro para internacionalización posterior.

## Review Focus
- Página maliciosa con botón falso «siguiente»: no activar por heurística ambigua.
- Evaluación/formulario: bloquear avance automático.
- Video finaliza después de cambio de documento: revocar modo automático.
- Motores de voz ausentes/idioma no disponible: degradación a botones accesibles.
- Elementos ocultos/disabled: excluirlos de navegación por foco.

---

### Task 1: Motor DOM con pruebas
**Files:** `shared/accessia.js`, `shared/tests/accessia.test.js`
**Interfaces:** `createEngine(document).execute(command: string): Promise<Result> | Result`; `Result = {ok:boolean,message:string}`.
- [x] Crear pruebas de comandos de video, navegación y guardas de evaluaciones.
- [x] Ejecutar pruebas con `node --test` y verificar resultados locales.
- [x] Implementar motor y validar invariantes.
- [ ] Revisar con usuarios y registrar hallazgos.

### Task 2: Aplicación Windows nativa
**Files:** `windows/Accessia.Windows/{Accessia.Windows.csproj,Program.cs}`
**Interfaces:** `ExecuteScriptAsync` ejecuta `window.Accessia.execute(...)` en documento activo.
- [x] Crear código fuente de interfaz y acciones.
- [ ] Construir con .NET SDK en Windows y probar en equipo real.
- [ ] Generar instalador firmado y probar accesibilidad de lectores de pantalla.

### Task 3: Aplicación Android nativa
**Files:** `android/app/src/main/**`, `android/*.gradle.kts`
**Interfaces:** GeckoView `runtime.webExtensionController`, `WebExtension.Port.postMessage`.
- [x] Crear código fuente y extensión empaquetada.
- [ ] Compilar mediante SDK 36/AGP y resolver incompatibilidades de API.
- [ ] Probar micrófono, foco, video y gestión de navegación en dispositivo real.

### Task 4: Validación institucional de LMS
**Files:** `docs/integraciones.md` (a crear tras permisos institucionales)
**Interfaces:** adaptador por sitio autorizado, no selector genérico intrusivo.
- [ ] Acordar plataforma piloto y autorización de integración.
- [ ] Añadir pruebas end-to-end en tres cursos demostrativos de la institución.
- [ ] Documentar compatibilidad por versión del LMS.

### Task 5: Despliegue y certificación
**Files:** `.github/workflows/`, `docs/`
**Interfaces:** build de PR + binarios de pre-release solo tras QA.
- [x] Crear repositorio público autenticado en GitHub (evidencia: registro Termux del autor 21/10/2026).
- [ ] Configurar runners, firmas, SBOM y gestión de incidencias.
- [ ] Ejecutar pruebas de usuario y auditoría WCAG/Play policies.
