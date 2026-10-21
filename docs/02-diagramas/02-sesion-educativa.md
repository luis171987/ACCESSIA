# D02 — Inicio y sesión educativa

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart TD
 START[Usuario inicia ACCESSIA] --> URL[Abre sitio educativo por voz o control]
 URL --> AUTH{Requiere cuenta del sitio}
 AUTH -->|Sí| USER[Usuario inicia sesión con credenciales propias]
 AUTH -->|No| READY[Lección visible]
 USER --> READY
 READY --> CHOOSE{Orden del usuario}
 CHOOSE -->|Pausar| PAUSE[Pausar reproducción]
 CHOOSE -->|Reanudar| PLAY[Reanudar reproducción]
 CHOOSE -->|Activar continuidad| CONSENT[Confirmación expresa]
 CONSENT --> VIDEO[Esperar fin real del video]
 VIDEO --> NEXT[Evaluar acción siguiente]
 NEXT --> CHOOSE
 CHOOSE -->|Cerrar| END[Detener micrófono y cerrar]
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
