# D04 — Video y continuidad educativa

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart TD
 V[Video HTML5 activo] --> CMD{Orden del usuario}
 CMD -->|Pausar| P[media.pause]
 CMD -->|Continuar| R[media.play]
 CMD -->|Estudio automático| C[Solicitar consentimiento]
 C -->|Rechazado| OFF[Modo automático desactivado]
 C -->|Aceptado| WAIT[Esperar evento ended]
 WAIT --> REAL{Video realmente terminó}
 REAL -->|No| WAIT
 REAL -->|Sí| CHECK[Comprobar evaluación, enlace, ambigüedad]
 CHECK -->|Seguro y único| NEXT[Activar Siguiente del mismo sitio]
 CHECK -->|No seguro| STOP[Detener y explicar en voz]
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
