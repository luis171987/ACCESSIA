# D05 — Evaluaciones, ambigüedad y consentimiento

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart TD
 END[Fin real de video] --> CONSENT{Consentimiento vigente en documento}
 CONSENT -->|No| STOP[No avanzar]
 CONSENT -->|Sí| EXAM{Examen, formulario o pago}
 EXAM -->|Sí| ASK[No responder; pedir intervención humana]
 EXAM -->|No| CAND{Siguiente seguro y unívoco}
 CAND -->|No| ASK
 CAND -->|Sí| ORIGIN{Permanece en sitio autorizado}
 ORIGIN -->|No| ASK
 ORIGIN -->|Sí| ACT[Continuar curso sin saltar contenido]
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
