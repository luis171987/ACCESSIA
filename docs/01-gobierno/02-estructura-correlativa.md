# 02 — Orden correlativo de carpetas y subcarpetas

```
ACCESSIA/
├── .github/                      [M06 · convención GitHub obligatoria]
│   └── workflows/               [CI: tests, Android, Windows]
├── android/                      [M03 · convención Gradle/Android]
│   ├── app/
│   │   └── src/main/            [convención técnica Android]
│   └── ...
├── windows/                      [M04 · convención .NET]
│   └── Accessia.Windows/
├── shared/                       [M02 · motor compartido]
│   ├── accessia.js
│   ├── demo/
│   └── tests/
├── tools/                        [M05/M06 · validaciones]
│   └── tests/
├── docs/                         [M01 · orden documental]
│   ├── 00-INDICE-MAESTRO.md
│   ├── 01-gobierno/              [01–03]
│   ├── 02-diagramas/             [D01–D12]
│   ├── 03-seguridad/             [01–02]
│   ├── 04-operaciones/           [01]
│   └── superpowers/             [especificación y plan existentes]
├── README.md
├── LICENSE
└── SECURITY.md
```

## Reglas

1. Para documentos: carpetas `NN-nombre/`, archivos `NN-tema.md`; para flujos: `DNN`, representados por `NN-tema.md` y enlazados desde el índice.
2. Para código: **conservar las convenciones oficiales del ecosistema**; cambiar `src/main`, `.github/workflows`, `android/app` o el nombre del proyecto C# rompería herramientas, flujos o documentación externa.
3. Las relaciones entre módulos, archivos y flujos se mantienen en la matriz de trazabilidad; no duplicar el motor JS en múltiples carpetas.
4. Los archivos privados no pertenecen a este árbol público. Claves SSH/firmas, tokens, contraseñas, cookies y perfiles de usuario permanecen fuera del repositorio.

**Equivalencia:** `M01` gobierno; `M02` JS; `M03` Android; `M04` Windows; `M05` QA; `M06` seguridad/entregas.
