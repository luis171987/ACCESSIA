# ACCESSIA — Navegador Inteligente de Accesibilidad Universal

<p align="center">
  <img src="docs/assets/accessia-logo-oficial.png" alt="Logotipo oficial aprobado de ACCESSIA, creado por GENAI INDUSTRIES" width="600" />
</p>

<p align="center"><sub><strong>Creado por GENAI INDUSTRIES</strong> · <strong>Creador y propietario único: Luis Carlos Moreno Jiménez</strong> · Santa Cruz de la Sierra, Bolivia</sub></p>

**Creado por GENAI INDUSTRIES. Creador y propietario único: Luis Carlos Moreno Jiménez.** Santa Cruz de la Sierra, Bolivia.

**Proyecto público y gratuito · Código fuente MIT · Android + Windows · Español inicial**


## 📥 Descargar ACCESSIA — instalación fácil

**No necesitas instalar GitHub, Termux ni herramientas de programación.** Abre el enlace de tu sistema operativo y descarga el archivo directamente desde la página oficial.

| Tu dispositivo | Descargar | Qué obtendrás |
|---|---|---|
| 📱 **Android 9 o posterior** | **[⬇️ Descargar ACCESSIA Beta 0.3 (.apk)](https://github.com/luis171987/ACCESSIA/releases/download/v0.3.0-beta.3/ACCESSIA-Android-Beta-0.3-DEBUG.apk)** | Aplicación experimental de 533 MB aprox.; abre el APK desde Descargas y sigue las instrucciones de Android. |
| 🖥️ **Windows 10/11 x64** | **[⬇️ Descargar ACCESSIA Windows portable (.zip)](https://github.com/luis171987/ACCESSIA/releases/download/v0.3.0-beta.3/ACCESSIA-Windows-Beta-portable.zip)** | Carpeta portable experimental: extrae **todo** el ZIP y abre `Accessia.Windows.exe`. No es instalador asistido. |

**[📦 Ver todas las versiones y archivos disponibles](https://github.com/luis171987/ACCESSIA/releases)** · **[📖 Tutorial paso a paso para Android y Windows](docs/GUIA_DESCARGA_E_INSTALACION.md)**

**Instalación en 3 pasos:** (1) descarga el APK de Android o ZIP de Windows; (2) en Android abre el APK y sigue el instalador; en Windows extrae el ZIP completo; (3) abre ACCESSIA. Si Android pide autorización para instalar desde el gestor de archivos, concédela solo para esta instalación y revócala después. En Windows podrías necesitar [**.NET 8 Desktop Runtime**](https://dotnet.microsoft.com/en-us/download/dotnet/8.0) y [**Microsoft Edge WebView2 Runtime**](https://developer.microsoft.com/en-us/microsoft-edge/webview2/consumer/).

> **Importante:** son versiones preliminares, sin firma de distribución de producción ni certificación de accesibilidad. La APK Android usa firma debug, que puede impedir actualizar una instalación anterior firmada de otra forma; **no la desinstales sin conservar tus datos**. El ZIP de Windows incluye un ejecutable, pero todavía no existe un instalador firmado `.exe` o `.msi` con asistente. No hay instaladores para iOS, macOS o Linux. Nunca confundas **Source code (zip)** con un instalador.

### 📣 Compartir ACCESSIA con su logotipo

Comparte esta dirección: **https://github.com/luis171987/ACCESSIA**. Para que **WhatsApp o Facebook** muestren el logotipo en la vista previa, el propietario debe subir la imagen oficial desde **GitHub → Settings → Social preview → Edit → Upload an image**; la imagen del README **no se utiliza automáticamente**. **[Abrir el logotipo original](https://raw.githubusercontent.com/luis171987/ACCESSIA/main/docs/assets/accessia-logo-oficial.png)** · [Tutorial para configurar la vista previa](docs/GUIA_DESCARGA_E_INSTALACION.md#cómo-hacer-que-aparezca-el-logotipo-al-compartir-el-repositorio). Algunas plataformas guardan versiones antiguas en caché.


> **Estado: prototipo experimental.** La Beta 0.3 de Android compiló en GitHub Actions y se publicó como [versión preliminar descargable](https://github.com/luis171987/ACCESSIA/releases/tag/v0.3.0-beta.3). Existe además un paquete portable experimental para Windows. El desarrollo de Android sigue en [revisión (PR #2)](https://github.com/luis171987/ACCESSIA/pull/2), sin fusionarse aún con `main`; no se certifica accesibilidad universal, funcionamiento plenamente manos libres ni funcionamiento correcto en todos los dispositivos.

ACCESSIA pretende facilitar la educación y navegación a personas con discapacidad motora. La automatización **sustituye acciones físicas**, pero no decisiones académicas. No resuelve exámenes, no salta videos y no elude CAPTCHA, DRM ni reglas de las plataformas.

## 🖼️ Interfaz de ACCESSIA en Android

Las siguientes **vistas ilustrativas** reproducen los elementos de interfaz definidos en el código Kotlin de la Beta v0.2: inicio, controles flotantes y ayuda por voz. **No son capturas de pantalla tomadas de un teléfono**; la apariencia final puede variar según Android, la resolución y las voces instaladas.

<p align="center">
  <img src="docs/assets/accessia-v02-vista-inicio.svg" alt="Vista ilustrativa del inicio de ACCESSIA con barra de búsqueda y acceso al micrófono" width="270" />
  <img src="docs/assets/accessia-v02-vista-controles.svg" alt="Vista ilustrativa del navegador ACCESSIA con panel de controles flotantes desplegado" width="270" />
  <img src="docs/assets/accessia-v02-vista-guia.svg" alt="Vista ilustrativa de la guía integrada de comandos de voz y accesibilidad" width="270" />
</p>

<p align="center"><sub>Inicio · Panel flotante · Guía de comandos</sub></p>

**[Ver implementación Android y avances de la Beta v0.2 (PR #2)](https://github.com/luis171987/ACCESSIA/pull/2)** · **[Consultar las imágenes de interfaz](docs/assets/)**

## 🌍 Voz natural, idiomas y traducción — actualización v0.3 en desarrollo

ACCESSIA está evolucionando para ser un navegador accesible **multilingüe** y admitir **dictado prolongado**. Las pruebas de campo evidencian que el reconocimiento por sesiones cortas puede truncar consultas y confundir palabras. La nueva rama de desarrollo incorpora un modo `Dictar +` para **acumular fragmentos en la barra de búsqueda y revisarlos antes de pulsar Ir**. Todavía requiere compilación y pruebas en el Honor.

**Idioma y localización:** al iniciar, se toma el idioma del dispositivo. El usuario puede seleccionar el idioma del reconocimiento y el idioma preferido que GeckoView solicita a las páginas, de una lista o mediante una etiqueta BCP 47. **Opcionalmente**, se ofrece una sugerencia aproximada por país de la IP pública, previa autorización expresa para consultar `ipapi.co`; no se persiste la IP y siempre puede rechazarse o cambiarse manualmente. **La IP no determina el idioma de una persona.**

**Traducción:** una opción permite elegir destino y, con confirmación, abrir la página mediante Google Traductor, que recibirá la URL. No usarla con páginas privadas o autenticadas; no todas las webs son traducibles. La traducción local e integrada, y la internacionalización **de todos los textos de la interfaz**, permanecen pendientes.

**Voz femenina humana, español latino:** seguimos evaluando proveedores, no se garantiza todavía una voz neuronal incluida. Entre las candidatas documentadas figuran `es-BO-SofiaNeural` y `es-MX-DaliaNeural` (Azure), y voces españolas de Piper para uso local; la elección final deberá comprobar calidad, licencias, privacidad y coste. No se incorporarán claves de proveedores al APK ni a GitHub.

**[Leer investigación, comparación de Vosk/sherpa-onnx/Piper, privacidad y plan de pruebas](https://github.com/luis171987/ACCESSIA/blob/feat/accessia-v02-ui-voice/docs/ACCESSIA_V03_VOZ_IDIOMAS_TRADUCCION.md)** · [Código en revisión (PR #2)](https://github.com/luis171987/ACCESSIA/pull/2).

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

## Pruebas locales

```bash
node --test shared/tests/*.test.js
python3 -m unittest discover -s tools/tests -p 'test_*.py'
python3 tools/check_public_secrets.py
```

## Compilar

- **Android**: abrir `android/` con Android Studio compatible con AGP 9.0.1, JDK 17, Android SDK 36 y GeckoView. Consultar `android/README.md`.
- **Windows**: Windows 10/11, .NET SDK 8, WebView2 Runtime; `dotnet build windows/Accessia.Windows/Accessia.Windows.csproj -c Release`. Consultar `windows/README.md`.
- **GitHub Actions**: `.github/workflows/` ejecuta pruebas y compilaciones de desarrollo. Un artefacto no equivale a instalador auditado o firmado.

## Cuentas, claves y privacidad

**ACCESSIA no incluye contraseñas, usuarios de demostración compartidos, claves SSH privadas ni tokens de acceso.** Cada persona usa sus propias cuentas de los sitios que visita; ACCESSIA no solicita la contraseña de GitHub del creador. Los perfiles web quedan bajo las protecciones de la cuenta del sistema operativo (no se promete aislamiento total frente a malware o un sistema comprometido).

NUNCA agregues `.env` real, `*.pem`, `*.key`, `id_rsa`, archivos de sesión, cookies, logs de autenticación ni certificados de firma al repositorio. Antes de publicar ejecuta `python3 tools/check_public_secrets.py`, inspecciona las diferencias y revisa GitHub secret scanning. Consulta [la política de credenciales](docs/03-seguridad/01-credenciales-por-usuario.md).

## Créditos y licencia

**ACCESSIA — creado por GENAI INDUSTRIES. Creador y propietario único: Luis Carlos Moreno Jiménez. Santa Cruz de la Sierra, Bolivia.** Código fuente licenciado bajo **MIT**: ver [LICENSE](LICENSE). MIT permite modificar, redistribuir e incluso comercializar derivados; el repositorio oficial se ofrece gratuitamente. La licencia del código no confiere por sí misma derechos de uso sobre la marca **GENAI INDUSTRIES** ni garantiza registro marcario. Las dependencias de terceros conservan sus propias licencias.

**Contacto y reportes de seguridad:** ver [SECURITY.md](SECURITY.md). No envíes contraseñas, capturas de cuentas ni datos personales en incidencias públicas.
