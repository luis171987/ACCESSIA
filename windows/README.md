# Windows — ACCESSIA prototipo

Requiere Windows 10/11, Microsoft WebView2 Runtime y SDK .NET 8 para compilación.

```
cd windows/Accessia.Windows
dotnet restore
dotnet build -c Release
dotnet publish -c Release -r win-x64 --self-contained false
```

La creación de un instalador `.exe` firmado es una actividad **posterior a QA**. `dotnet publish` genera una aplicación para Windows, pero no un instalador asistido ni una distribución certificada.

El reconocimiento de voz depende de la instalación del motor de español de Windows (System.Speech). No hay motor garantizado para todos los equipos; sin él permanece disponible la interfaz de botones accesibles. Los comandos de 'estudio automático' en voz exigen confirmación verbal adicional. Esta versión no accede al teclado global ni controla otras apps.

> NOTA: este proyecto no ha sido compilado en Windows desde el entorno de creación. Requiere corrección de posibles incompatibilidades tras primera compilación real.
