# 01 — Política de credenciales propias por usuario

**Autoría:** GENAI INDUSTRIES — Luis Carlos Moreno Jiménez — Santa Cruz de la Sierra, Bolivia.

## Principio

ACCESSIA **no provee cuentas predeterminadas ni credenciales del creador** para servicios de terceros. Cada estudiante inicia sesión en sus propios servicios a través de la página web legítima, HTTPS y mecanismos autorizados. ACCESSIA no debe pedir ni exportar la contraseña para la plataforma y no se debe registrar audio con datos sensibles sin consentimiento informado.

## Separación de perfiles

- **Android**: GeckoView usa el perfil privado de la aplicación bajo el aislamiento de Android; `allowBackup=false`. Otros usuarios de Android deben utilizar su propio perfil de sistema, sujeto a capacidades del dispositivo.
- **Windows**: WebView2 se inicializa en `%LOCALAPPDATA%\GENAI INDUSTRIES\ACCESSIA\WebView2` de la cuenta que ejecuta la aplicación. Cuentas de Windows diferentes obtienen carpetas diferentes por permisos del sistema. Usuarios que compartan **la misma cuenta de Windows** también comparten el perfil: requerir aislamiento adicional antes de vender esto como multisesión.
- **GitHub del creador**: `gh auth login` autentica la publicación del código, NO forma parte del navegador. El aviso de `gh` sobre credenciales guardadas en texto claro obliga a proteger Termux y a cerrar sesión (`gh auth logout`) si no se necesita mantenerla.
- **No sincronización**: ACCESSIA no implementa un backend central de cuentas. La voz puede procesarse mediante servicios externos del sistema operativo según la configuración de la persona y del reconocedor.

## Qué nunca debe publicarse

Claves privadas SSH, credenciales de firma Android, archivos `.env` reales, `*.pem`, `*.p12`, `*.pfx`, `*.key`, cookies, capturas de autenticación, logs con tokens, respaldos de perfiles, tokens de GitHub, claves API, contraseñas, datos personales de estudiantes.

## Uso de secretos en compilación

Usar GitHub Actions Secrets con permisos mínimos cuando existan firmas finales. No incluir esos secretos en repositorios, artefactos ni logs. El proyecto actual utiliza builds de desarrollo SIN firma de producción. El escáner local es una heurística: no puede acreditar ausencia total de datos sensibles.

## Aviso a usuarios

Mostrar una política clara sobre almacenamiento local, tratamiento del micrófono, posible reconocimiento online del SO y eliminación de datos. Evitar prometer cifrado propio en esta versión: las protecciones dependen de Android y Windows.
