# 02 — Auditoría de código público y respuesta a incidentes

## Antes de subir cambios

1. Confirmar que el directorio de trabajo es el repositorio previsto: `git remote -v` y `gh repo view luis171987/ACCESSIA --json name,visibility,url` (sin imprimir tokens).
2. Ejecutar `python3 tools/check_public_secrets.py` y las pruebas del detector.
3. Revisar los nombres de archivos por subir con `git status --short` y examinar `git diff --cached --stat`; antes de `git add`, no emplear `git add -A` sin revisar cuando exista material personal.
4. Revisar el historial: `git log --oneline --all` y activar **GitHub Secret Scanning y Push Protection** cuando esté disponible para el repositorio/cuenta. El escáner de este proyecto NO inspecciona retrospectivamente commits borrados.
5. Exigir revisión de cambios externos y permisos mínimos de CI (`contents: read`).

## Ante una filtración

**No basta con borrar el archivo del último commit.** Revocar o rotar de inmediato la credencial en el proveedor, detener despliegues afectados, restringir acceso, sanear historial con procedimiento controlado y revisar forks, logs y artefactos. Nunca copiar el valor filtrado a incidencias públicas o al chat. Si afectan a usuarios, activar el proceso de notificación aplicable.

## Alcance de las comprobaciones

El escáner heurístico comprueba tokens con formatos reconocibles, encabezados de claves privadas y asignaciones peligrosas. No detecta todas las contraseñas, secretos cifrados, imágenes con credenciales ni historial remoto. Una pasada sin hallazgos NO certifica seguridad del software.
