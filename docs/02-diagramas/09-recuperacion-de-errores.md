# D09 — Errores y recuperación accesible

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart TD
 ACTION[Acción solicitada] --> CHECK{¿Elementos y motor disponibles?}
 CHECK -->|Sí| EXEC[Ejecutar con límites]
 EXEC --> RESULT{¿Acción confirmada?}
 RESULT -->|Sí| FEEDBACK[Comunicar resultado por voz y texto]
 RESULT -->|No| STOP[Cancelar continuidad y explicar fallo]
 CHECK -->|No| STOP
 STOP --> OPTIONS[Ofrecer repetir, navegación por foco o ayuda humana]
 OPTIONS --> RETRY{Usuario elige reintentar}
 RETRY -->|Sí| ACTION
 RETRY -->|No| SAFE[Conservar control humano]
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
