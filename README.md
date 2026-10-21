# ACCESSIA — Navegador Inteligente de Accesibilidad Universal

<p align="center">
  <img src="docs/assets/accessia-logo-oficial.png" alt="Logo oficial aprobado de ACCESSIA" width="540" />
</p>

<p align="center"><sub><strong>Creado por GENAI INDUSTRIES</strong> · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia</sub></p>


**Creado por GENAI INDUSTRIES**, de **Luis Carlos Moreno Jiménez**, Santa Cruz de la Sierra, Bolivia.

**Proyecto público y gratuito · Código fuente MIT · Android + Windows · Español inicial**

> **Estado: prototipo de desarrollo.** La APK debug del commit `d74f8f0` compiló en GitHub Actions y fue instalada en un Honor Magic 7 Lite el 21/10/2026. Se reportaron problemas de rendimiento y reconocimiento de voz. La actualización v0.2 está **pendiente de CI y validación de campo**; no constituye certificación de accesibilidad ni disponibilidad universal.

ACCESSIA pretende facilitar la educación y navegación a personas con discapacidad motora. La automatización **sustituye acciones físicas**, pero no decisiones académicas. No resuelve exámenes, no salta videos y no elude CAPTCHA, DRM ni reglas de las plataformas.

## Acceso rápido y orden correlativo

| ID | Módulo | Carpeta técnica | Documentación/diagrama |
|---|---|---|---|
| M01 | Gestión, identidad y alcance | `docs/01-gobierno/` | [01 Identidad](docs/01-gobierno/01-identidad-y-licencia.md) |
| M02 | Motor compartido de accesibilidad | `shared/` | [D03 Navegación](docs/02-diagramas/03-navegacion-dom.md) |
| M03 | Cliente Android | `android/` | [D07 Plataformas](docs/02-diagramas/07-plataformas-y-datos.md) |
| M04 | Cliente Windows | `windows/` | [D07 Plataformas](docs/02-diagramas/07-plataformas-y-datos.md) |
| M05 | Pruebas y calidad | `shared/tests/`, `tools/tests/` | [D08 Integración continua](docs/02-diagramas/08-ci-cd-y-distribucion.md) |
| M06 | Seguridad, privacidad y publicación | `docs/03-seguridad/`, `tools/`, `.github/` | [D06 Credenciales](docs/02-diagramas/06-credenciales-y-privacidad.md) |

**[📚 ÍNDICE MAESTRO DE DOCUMENTOS Y 12 DIAGRAMAS](docs/00-INDICE-MAESTRO.md)** · [Mapa de carpetas](docs/01-gobierno/02-estructura-correlativa.md) · [Estado verificable](docs/ESTADO_ACTUAL.md) · [Guía de seguridad](docs/03-seguridad/01-credenciales-por-usuario.md).

Las carpetas técnicas conservan nombres estándar (`.github`, `android/app/src/main`, `windows/`, `shared/`) para no romper las herramientas de compilación; su orden correlativo se expresa con los identificadores **M01–M06** y con documentos **D01–D12**, no con renombres artificiales incompatibles.

## Características implementadas en el prototipo

- Motor JavaScript compartido `shared/accessia.js`: foco, selección, video HTML5, continuidad explícita limitada al documento y bloqueo ante evaluaciones o navegación ambigua.
- Android: fuente Kotlin/GeckoView, voz experimental en primer plano y controles accesibles.
- Windows: fuente C#/WinForms/WebView2, reconocimiento de voz según motores disponibles, perfil del navegador separado por cuenta de Windows.
- Demo educativa y pruebas unitarias Node. **Los seis agentes especializados son roles de arquitectura, no seis modelos autónomos implementados.**

Comandos: `pausar`, `continuar`, `cursor siguiente`, `cursor anterior`, `seleccionar`, `activar estudio automático`, `desactivar estudio automático`, `cerrar página`, `cerrar navegador`, `acerca de` (créditos).

## Mejoras propuestas en ACCESSIA v0.2 (rama de revisión)

- Identidad oficial: logo original aprobado e icono del lanzador Android.
- Interfaz nativa azul marino/cian, cabecera compacta, menú, botón flotante y controles desplegables opcionales.
- Voz: errores de escucha más explicativos, reintentos controlados, idioma seleccionable y frases de búsqueda largas.
- Funciones locales: favoritos, historial, limpieza de historial, recarga y comando `salir`.
- Descargas: acceso al gestor de descargas Android. **Descargas automáticas desde GeckoView aún no implementadas.**
- Inicio sin página remota precargada, para reducir la latencia percibida.

[Plan de validación Android v0.2](docs/ACCESSIA_V02_PRUEBAS.md) · [Seguimiento funcional](https://github.com/luis171987/ACCESSIA/issues/1).

## Pruebas locales

```bash
node --test shared/tests/*.test.js
python3 -m unittest discover -s tools/tests -p 'test_*.py'
python3 tools/check_public_secrets.py
```

## Compilar

- **Android**: abrir `android/` con Android Studio compatible con AGP 9.4.0, JDK 17, Android SDK 37.2 y GeckoView Nightly fijado en Gradle. Consultar `android/README.md`.
- **Windows**: Windows 10/11, .NET SDK 8, WebView2 Runtime; `dotnet build windows/Accessia.Windows/Accessia.Windows.csproj -c Release`. Consultar `windows/README.md`.
- **GitHub Actions**: `.github/workflows/` ejecuta pruebas y compilaciones de desarrollo. Un artefacto no equivale a instalador auditado o firmado.

## Cuentas, claves y privacidad

**ACCESSIA no incluye contraseñas, usuarios de demostración compartidos, claves SSH privadas ni tokens de acceso.** Cada persona usa sus propias cuentas de los sitios que visita; ACCESSIA no solicita la contraseña de GitHub del creador. Los perfiles web quedan bajo las protecciones de la cuenta del sistema operativo (no se promete aislamiento total frente a malware o un sistema comprometido).

NUNCA agregues `.env` real, `*.pem`, `*.key`, `id_rsa`, archivos de sesión, cookies, logs de autenticación ni certificados de firma al repositorio. Antes de publicar ejecuta `python3 tools/check_public_secrets.py`, inspecciona las diferencias y revisa GitHub secret scanning. Consulta [la política de credenciales](docs/03-seguridad/01-credenciales-por-usuario.md).

## Créditos y licencia

**ACCESSIA — creado por GENAI INDUSTRIES, de Luis Carlos Moreno Jiménez, Santa Cruz de la Sierra, Bolivia.** Código fuente licenciado bajo **MIT**: ver [LICENSE](LICENSE). MIT permite modificar, redistribuir e incluso comercializar derivados; el repositorio oficial se ofrece gratuitamente. La licencia del código no confiere por sí misma derechos de uso sobre la marca **GENAI INDUSTRIES** ni garantiza registro marcario. Las dependencias de terceros conservan sus propias licencias.

**Contacto y reportes de seguridad:** ver [SECURITY.md](SECURITY.md). No envíes contraseñas, capturas de cuentas ni datos personales en incidencias públicas.
