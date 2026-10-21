# ACCESSIA — Navegador Inteligente de Accesibilidad Universal

**Proyecto gratuito y de código abierto (MIT).** Origen: Santa Cruz de la Sierra, Bolivia. Propuesto por Luis Carlos Moreno Jiménez. Repositorio previsto: `luis171987/ACCESSIA`.

> **ESTADO: prototipo de desarrollo — NO es una versión pública validada ni un producto certificado.**
> No se han generado ni verificado APK/EXE. El código requiere builds y pruebas en Android/Windows.

ACCESSIA busca facilitar la navegación por Internet y la continuidad de cursos a personas con discapacidad motora. El estudiante mantiene el control de las actividades académicas.

## Qué incluye este repositorio

- `shared/accessia.js`: motor de navegación DOM, foco accesible, control de video HTML5 y continuidad autorizada de lecciones.
- `shared/tests/`: pruebas automáticas sin dependencias externas usando Node.js.
- `shared/demo/`: demostración local, **no sustituye pruebas en plataformas educativas reales**.
- `android/`: prototipo nativo Kotlin/GeckoView, comandos de voz mientras está en primer plano y extensión integrada.
- `windows/`: prototipo nativo C#/Windows Forms/WebView2, botones accesibles y reconocimiento de voz si Windows tiene motor instalado.
- `docs/`: arquitectura, limitaciones, planes de mejora y seguridad.

## Acciones principales

`cerrar página`, `cerrar navegador`, `pausar`, `continuar`, `reproducir`, `siguiente`, `anterior`, `seleccionar`, `bajar`, `subir`, `activar estudio automático`, `desactivar estudio automático`, `siguiente lección`.

La función «estudio automático» requiere activación explícita en cada página. No adelanta videos, contesta evaluaciones ni elude restricciones de sitios o mecanismos antibots. Si la página parece una evaluación o no se identifica inequívocamente la continuación, se detiene.

## Pruebas verificables (sin SDK)

```bash
cd shared
node --test tests/*.test.js
```

## Android (fuente)

Abrir `android/` con Android Studio que soporte AGP 9.0.1 y JDK 17. Configurar el SDK Android 36. Compilar `assembleDebug` con Gradle de versión compatible. El paquete GeckoView se descarga del Maven oficial de Mozilla; consultar `android/app/build.gradle.kts` para la versión elegida y actualizarla si es necesario. El micrófono exige permiso explícito. La escucha experimental funciona solo en primer plano, depende del servicio de reconocimiento disponible y **no garantiza manos libres continuo**.

## Windows (fuente)

Abrir `windows/Accessia.Windows/Accessia.Windows.csproj` en Windows 10/11 con .NET SDK 8 y WebView2 Runtime; compilar con `dotnet build` y probar. El reconocimiento depende de un motor de idioma instalado en Windows; si no está disponible, la navegación por botones sigue operativa.

## Publicación del código

**No se ha creado el repositorio remoto ni se ha realizado push a GitHub desde esta conversación.** Para publicarlo como proyecto público, crear `ACCESSIA` en la cuenta `luis171987` con visibilidad `Public`, sin inicializar, y subir todos los archivos de este ZIP, o usar GitHub Desktop/CLI para `git push` con tu propia autenticación. Nunca incluyas tokens o credenciales en el repositorio.

## Referencias técnicas

- Mozilla GeckoView: https://firefox-source-docs.mozilla.org/mobile/android/geckoview/
- Microsoft WebView2: https://learn.microsoft.com/en-us/microsoft-edge/webview2/
- W3C WCAG: https://www.w3.org/TR/WCAG22/
- Cloudflare Verified Bots: https://developers.cloudflare.com/bots/concepts/bot/verified-bots/

## Licencia

MIT. Ver `LICENSE`. GeckoView, WebView2 y sus bibliotecas conservan sus propias licencias y avisos legales.
