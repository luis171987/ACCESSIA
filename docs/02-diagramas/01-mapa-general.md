# D01 — Mapa general del sistema

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart TD
 U[Persona usuaria] --> I[Interfaz accesible y comandos]
 I --> P{Plataforma}
 P --> A[Android · Kotlin y GeckoView]
 P --> W[Windows · C# y WebView2]
 A --> S[M02 · Motor compartido JavaScript]
 W --> S
 S --> DOM[Controles DOM y videos HTML5]
 DOM --> WEB[Sitio educativo autorizado]
 S --> G[Supervisor de acciones y bloqueos]
 G --> OK[Acción accesible autorizada]
 G --> STOP[Detener y explicar]
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
