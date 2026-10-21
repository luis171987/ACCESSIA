# D11 — Primer uso y acceso sin manos

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart TD
 INSTALL[Instalar aplicación] --> PERM[Explicar permisos y micrófono]
 PERM --> OPT{¿Reconocedor de voz disponible?}
 OPT -->|Sí| ENABLE[Usuario habilita voz de forma explícita]
 OPT -->|No| ALT[Ofrecer Voice Access, lector de pantalla o pulsador]
 ENABLE --> GUIDE[Anunciar comandos disponibles]
 ALT --> GUIDE
 GUIDE --> OPEN[Abrir curso y elegir lección]
 OPEN --> PRACTICE[Probar pausar, continuar, cursor y seleccionar]
 PRACTICE --> AUTO{¿Solicita continuidad?}
 AUTO -->|Sí| CONF[Confirmación para este documento]
 AUTO -->|No| MAN[Modo manual asistido]
 CONF --> STUDY[Sesión educativa]
 MAN --> STUDY
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
