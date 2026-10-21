# 03 — Matriz de trazabilidad: módulos ↔ flujos ↔ archivos ↔ validación

| Módulo | Diagramas | Archivos principales | Evidencia automática | Pendientes |
|---|---|---|---|---|
| M01 Gobierno | D01, D12 | `README.md`, `LICENSE`, `docs/01-gobierno/` | prueba de enlaces y créditos | revisión editorial |
| M02 Motor | D02–D05, D09 | `shared/accessia.js`, `shared/tests/` | Node `--test` | probar sitios reales con consentimiento |
| M03 Android | D07, D11 | `android/app/src/main/` | workflow Android cuando pase | APK, TalkBack, voz, Android 16 |
| M04 Windows | D07, D11 | `windows/Accessia.Windows/` | workflow Windows cuando pase | EXE, lectores de pantalla, perfiles Windows |
| M05 Calidad | D08, D09, D12 | `.github/workflows/`, `tools/tests/` | workflows + unittest | pruebas con personas con discapacidad |
| M06 Seguridad | D05–D08 | `.gitignore`, `SECURITY.md`, `docs/03-seguridad/`, `tools/check_public_secrets.py` | detector heurístico local/CI | scanning GitHub, auditoría histórica, SBOM, firma |

**Advertencia:** documentación y pruebas de unidad no prueban compilaciones nativas ni conformidad WCAG. Registrar cada resultado de CI antes de cambiar el estado.
