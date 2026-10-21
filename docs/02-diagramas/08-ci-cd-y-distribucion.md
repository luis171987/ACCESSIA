# D08 — Integración continua y distribución

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart TD
 DEV[Contribución o commit] --> REVIEW[Revisión de cambios]
 REVIEW --> S[Escáner heurístico de secretos y tests del escáner]
 S --> FOUND{Hallazgos}
 FOUND -->|Sí| BLOCK[Bloquear hasta corregir]
 FOUND -->|No| TEST[Test unitarios Node]
 TEST --> BUILD[Workflows compilación Android y Windows]
 BUILD --> STATUS{Compilación exitosa}
 STATUS -->|No| FIX[Registrar fallo y corregir]
 FIX --> REVIEW
 STATUS -->|Sí| QA[Pruebas de dispositivos, seguridad, accesibilidad y firma]
 QA --> READY{Aprobación de distribución}
 READY -->|No| DEVONLY[Artefactos internos de desarrollo]
 READY -->|Sí| RELEASE[Publicación de instaladores auditados]
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
