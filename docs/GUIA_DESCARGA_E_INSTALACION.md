# 📥 Cómo descargar e instalar ACCESSIA — guía para principiantes

> **Proyecto experimental gratuito.** La publicación `v0.3.0-beta.3` incluye una versión **Android de prueba (APK debug)**; Windows se distribuye como paquete portable **únicamente si el archivo ZIP aparece en la sección Assets**. No hay instaladores comerciales firmados ni aplicaciones para iOS, macOS o Linux.

## Enlace oficial de descargas

**[Abrir la página de descargas de ACCESSIA (GitHub Releases)](https://github.com/luis171987/ACCESSIA/releases/tag/v0.3.0-beta.3)**

En esta página, busca el apartado **Assets** (en móvil puede estar plegado): allí están los archivos disponibles. **No descargues `Source code (zip)`** pensando que es una aplicación: es el código fuente, no un instalador.

### 📱 Android — instalar ACCESSIA Beta 0.3

1. Desde el teléfono Android, pulsa **[Descargar APK de Android](https://github.com/luis171987/ACCESSIA/releases/download/v0.3.0-beta.3/ACCESSIA-Android-Beta-0.3-DEBUG.apk)**.
2. Si el navegador pregunta si deseas descargar el archivo `.apk`, confirma únicamente si estás en el repositorio oficial `luis171987/ACCESSIA`.
3. Abre **Archivos / Mis archivos → Descargas** y selecciona `ACCESSIA-Android-Beta-0.3-DEBUG.apk`.
4. Si Android solicita permiso para **Instalar aplicaciones desconocidas** en tu navegador o gestor de archivos, concédelo solo para esa instalación y desactívalo después. No desactives Play Protect.
5. Elige **Instalar** y abre **ACCESSIA Beta 0.3**. La primera vez permite el micrófono si quieres usar comandos de voz.
6. Prueba navegación, controles y `Dictar +`. El servicio de reconocimiento de voz depende del proveedor de Android; no se garantiza escucha continua ni voz neuronal.

**Requisitos:** Android 9 o posterior, arquitectura compatible y espacio libre suficiente para una APK aproximada de 533 MB y su instalación. Las compilaciones de prueba pueden tener firmas diferentes. Si aparece «Aplicación no instalada» o «Conflicto de paquete», **no desinstales una versión anterior sin antes conservar los datos**; informa el error a los mantenedores.

**Seguridad:** archivo experimental no firmado como lanzamiento de producción, no disponible en Play Store y no auditado para introducir datos sensibles. Descarga solo desde el repositorio oficial. Puedes contrastar su SHA-256 con:

```text
95a0c164a58159978700ee6146f9ce0eaa77c5de664b496387f03ac6d1063a96
```

### 🖥️ Windows — versión portable de prueba

1. En un PC con Windows x64, abre **[la página de descargas](https://github.com/luis171987/ACCESSIA/releases/tag/v0.3.0-beta.3)**.
2. En **Assets**, si está disponible, descarga `ACCESSIA-Windows-Beta-portable.zip`. **Si ese archivo no aparece, Windows aún no se ha publicado.**
3. En Descargas, selecciona el ZIP y pulsa **Extraer todo**. No intentes ejecutar la aplicación desde dentro del ZIP.
4. En la carpeta extraída, abre `Accessia.Windows.exe`. **No es un instalador asistido ni una aplicación firmada.**
5. Si Windows solicita dependencias, instala desde sus proveedores oficiales **[.NET 8 Desktop Runtime](https://dotnet.microsoft.com/en-us/download/dotnet/8.0)** y **[Microsoft Edge WebView2 Runtime](https://developer.microsoft.com/en-us/microsoft-edge/webview2/consumer/)**.
6. No ignores advertencias de Windows SmartScreen sin comprobar el origen y la integridad del archivo. Como es una beta no certificada, úsala solo para pruebas.

El acceso por voz en Windows requiere que exista un motor de reconocimiento de voz compatible en el sistema. La compilación `dotnet publish` genera un paquete portable; **un instalador asistido `.exe` o `.msi` firmado continúa pendiente**.

### Otros sistemas operativos

**iPhone/iPad (iOS), macOS y Linux:** no existen instaladores publicados actualmente. No uses archivos APK en iPhone o Windows ni archivos Windows en Android.

## Cómo descargar sin conocer GitHub

- **Releases / Versiones:** lugar correcto para obtener aplicaciones y archivos preparados.
- **Assets / Activos:** archivos descargables de cada versión (APK, ZIP, etc.).
- **Source code (zip):** solo el código fuente, no una APK ni un instalador Windows.
- **Code / Código → Download ZIP:** descarga el código para desarrolladores.
- **Actions / Acciones:** compilaciones técnicas y temporales; sus artefactos pueden requerir una cuenta GitHub y caducar. No son la vía recomendada para usuarios finales.

## Cómo hacer que aparezca el logotipo al compartir el repositorio

**Solo el propietario o un administrador del repositorio puede configurarlo:** el logotipo dentro de `README.md` **no establece automáticamente la vista previa social**.

1. Entra con tu cuenta al [repositorio oficial ACCESSIA](https://github.com/luis171987/ACCESSIA).
2. Abre **Settings / Configuración** (en móvil puede estar detrás de un menú).
3. Busca **Social preview / Vista previa social** y selecciona **Edit → Upload an image**.
4. Sube **[el logotipo oficial original de ACCESSIA](https://raw.githubusercontent.com/luis171987/ACCESSIA/main/docs/assets/accessia-logo-oficial.png)**, sin sustituirlo por ilustraciones ajenas.
5. Guarda los cambios y comparte la URL principal: `https://github.com/luis171987/ACCESSIA`.

GitHub recomienda PNG, JPG o GIF de **menos de 1 MB** y proporción aproximada **2:1** (óptimo 1280 × 640). El PNG oficial del repositorio ocupa **974.249 bytes**; puede utilizarse si GitHub acepta sus dimensiones, aunque una imagen cuadrada podría recortarse. Si necesitas una versión panorámica, debe conservarse intacto el logotipo autorizado. WhatsApp y Facebook pueden conservar imágenes anteriores en caché y no siempre actualizan la previsualización inmediatamente.

Documentación: [vista previa social de GitHub](https://docs.github.com/es/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/customizing-your-repositorys-social-media-preview).

---

**Soporte y seguridad:** [informar problemas y vulnerabilidades](../SECURITY.md) · [repositorio oficial](https://github.com/luis171987/ACCESSIA)

**Estado:** guía de instalación de prototipos, no certificación ni promesa de disponibilidad de archivos todavía no publicados.
