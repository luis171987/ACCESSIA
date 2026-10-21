# Publicación pública en GitHub desde Honor Magic 7 Lite / Termux

> El repositorio remoto aún NO ha sido creado por este paquete.

1. Descargar `ACCESSIA_SOURCE_v0.1.0.zip` a **Descargas** del teléfono.
2. En Termux (que tenga acceso a `/sdcard/Download`), pegar:

```bash
pkg install git gh unzip -y
cd "$HOME"
unzip -o "/sdcard/Download/ACCESSIA_SOURCE_v0.1.0.zip"
cd "$HOME/ACCESSIA"
bash PUBLICAR_GITHUB_TERMUX.sh
```

3. GitHub CLI mostrará un inicio de sesión en tu navegador autorizado. **No copies claves ni códigos privados dentro del chat.**
4. El script revisa que estás autenticado como `luis171987`. Si la cuenta no coincide, cancela.
5. El script crea `ACCESSIA` con visibilidad **pública** y sube el código. Si el repositorio ya existía, se detiene antes de sobrescribir.
6. Verificar la publicación directamente en `https://github.com/luis171987/ACCESSIA` una vez que el comando informe éxito. La URL no prueba que ya exista.

**Alternativa en interfaz web:** visita `https://github.com/new`, crea `ACCESSIA`, selecciona `Public` y utiliza el procedimiento oficial de GitHub para subir carpetas/archivos descomprimidos. Subir solo el ZIP lo guardaría como un archivo, no como código fuente navegable.

Los workflows de compilación dependen de descargas de Android SDK/Gradle/GeckoView y .NET/NuGet. Pueden requerir ajustes tras el primer CI. Los APK/EXE de CI son artefactos de desarrollo, no versiones finales auditadas.
