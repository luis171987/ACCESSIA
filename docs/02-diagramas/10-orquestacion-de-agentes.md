# D10 — Agentes propuestos y realidad técnica

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart TD
 U[Comando del usuario] --> S[Supervisor de permisos y límites]
 S --> VOZ[Rol: voz · reconocimiento del SO]
 S --> NAV[Rol: navegador · cliente nativo]
 S --> FOCUS[Rol: navegación · motor JS]
 S --> EDU[Rol: educación · motor JS]
 S --> A11Y[Rol: accesibilidad · interfaz y TTS]
 VOZ --> S
 NAV --> S
 FOCUS --> S
 EDU --> S
 A11Y --> S
 S --> CHECK{¿Implementado, permitido y seguro?}
 CHECK -->|Sí| ACTION[Acción trazable y anuncio]
 CHECK -->|No| LIMIT[Informar límite o requerir consentimiento]
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
