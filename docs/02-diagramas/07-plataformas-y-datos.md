# D07 — Android, Windows y motor compartido

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart LR
 A[Android · APK futuro] --> AR[GeckoView y puente WebExtension]
 W[Windows · EXE futuro] --> WR[WebView2 y puente controlado]
 AR --> CORE[Motor JS M02]
 WR --> CORE
 CORE --> DOM[DOM y HTML5 del sitio]
 AR --> AS[Perfil de la app Android · datos privados]
 WR --> WS[Perfil WebView2 en AppData local por usuario]
 AS -. Sin sincronización propia .-> N[No hay nube ACCESSIA por defecto]
 WS -. Sin sincronización propia .-> N
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
