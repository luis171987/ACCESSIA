# D06 — Credenciales y privacidad

**ACCESSIA · GENAI INDUSTRIES · Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia**
**Formato:** Mermaid editable. **Estado:** diseño / prototipo; no garantiza que todas las rutas estén implementadas o validadas.
[Volver al índice maestro](../00-INDICE-MAESTRO.md)

```mermaid
flowchart TD
 U[Persona usuaria] --> SITE[Abre sitio HTTPS]
 SITE --> LOGIN{Sitio requiere cuenta}
 LOGIN -->|Sí| INPUT[Introducción de credenciales personales]
 INPUT --> PROFILE[Almacenamiento del perfil del navegador del SO]
 LOGIN -->|No| BROWSE[Navegación]
 PROFILE --> BROWSE
 BROWSE --> PUBLIC{¿Publicar código a GitHub?}
 PUBLIC -->|Sí| CHECK[Auditar árbol e historial en busca de secretos]
 CHECK --> BAD{¿Hay datos sensibles?}
 BAD -->|Sí| BLOCK[Cancelar publicación, rotar secretos y sanear historial]
 BAD -->|No| PUSH[Publicar SOLO código y documentación]
 PUBLIC -->|No| END[Conservar datos fuera de GitHub]
```

Consulte [la matriz de trazabilidad](../01-gobierno/03-matriz-trazabilidad.md) para identificar archivos, pruebas y brechas entre diseño e implementación.
