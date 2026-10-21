# 01 — Validación y publicación responsable

**Requisitos locales:** Git, Python 3, Node 22. Los SDK Android/.NET solo para compilaciones de las aplicaciones.

```bash
cd ~/ACCESSIA
python3 tools/check_public_secrets.py
python3 -m unittest discover -s tools/tests -p 'test_*.py'
node --test shared/tests/*.test.js
git diff --check
gh run list -R luis171987/ACCESSIA --limit 10
```

Antes de `git push`, comprueba en GitHub el repositorio y la rama. Los workflows de Android/Windows pueden fallar por dependencias/API; no declarar instaladores probados hasta verificar compilación, pruebas reales, accesibilidad y firma.

**Orden de salida:** revisión código → escaneo de secretos → pruebas JS → compilación Android/Windows → pruebas de voz/lector de pantalla → validación con estudiantes → verificación legal/privacidad → binarios públicos.

[Índice maestro](../00-INDICE-MAESTRO.md) · [Matriz](../01-gobierno/03-matriz-trazabilidad.md).
