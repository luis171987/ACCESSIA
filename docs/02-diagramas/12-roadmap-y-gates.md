# D12 — Hoja de ruta y validaciones obligatorias

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart TD
 SPEC[Diseño y autoría documentados] --> CORE[Motor compartido y pruebas unitarias]
 CORE --> APP[Fuente Android + Windows]
 APP --> CI{¿Compilan ambos sistemas?}
 CI -->|No| FIX[Corregir SDK, APIs y dependencias]
 FIX --> CI
 CI -->|Sí| REAL[Pruebas reales con personas y accesibilidad]
 REAL --> QA{¿Cumple criterios de seguridad y autonomía?}
 QA -->|No| FIX
 QA -->|Sí| SIG[Auditoría, SBOM, privacidad, firma]
 SIG --> RELEASE[Releases públicos aprobados]
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
