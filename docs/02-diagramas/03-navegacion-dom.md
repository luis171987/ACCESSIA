# D03 — Selección de controles DOM

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart TD
 C[Comando: cursor siguiente o anterior] --> SCAN[Enumerar elementos accesibles]
 SCAN --> FILTER[Excluir invisibles, inertes y deshabilitados]
 FILTER --> EMPTY{Quedan controles}
 EMPTY -->|No| SAY[Anunciar que no hay controles]
 EMPTY -->|Sí| FOCUS[Mover foco y comunicar nombre]
 FOCUS --> CH{Usuario dice seleccionar}
 CH -->|No| HOLD[Esperar otra orden]
 CH -->|Sí| CLICK[Activar únicamente el control enfocado]
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
