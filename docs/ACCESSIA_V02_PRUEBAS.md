# ACCESSIA v0.2 — Plan de validación en Android

**Creado por GENAI INDUSTRIES — Luis Carlos Moreno Jiménez, Santa Cruz de la Sierra, Bolivia.**

Estado: parche experimental, pendiente de compilación GitHub Actions y pruebas en dispositivo.

## Comprobaciones manuales

1. Abrir ACCESSIA desde el cajón de aplicaciones. Si no aparece, comprobar el lanzador, el perfil Android y el permiso de iconos.
2. Verificar el icono azul aprobado y la firma corporativa discreta en la cabecera.
3. Primera apertura: no debe cargar Wikipedia por defecto. Escribir una URL HTTPS y una búsqueda de 8 o más palabras.
4. Pulsar `☰`: comprobar panel flotante, botones grandes y opción de ocultar. Desde el menú `⋯` desactivar y reactivar el botón flotante.
5. Guardar una URL en `★ Favorito`, verla en `Favoritos`, reabrirla y eliminarla. Cerrar y reabrir app; debe persistir.
6. Visitar dos URL HTTPS; comprobar historial, navegación y limpieza con confirmación.
7. Verificar que `Descargas` abre el gestor del sistema. **Este parche no implementa todavía la descarga automática de contenidos de GeckoView.**
8. Dar permiso de micrófono y dictar despacio, normal y rápido: `buscar bibliotecas digitales de Bolivia y accesibilidad universitaria`.
9. Probar `salir`, `atras`, `recargar`, `guardar en favoritos`, `mostrar botones`, `ocultar botones`, `historial` y `descargas`.
10. Si no reconoce, cambiar el idioma entre Español de Bolivia, España y México. Anotar proveedor de reconocimiento instalado y códigos de error observados.
11. Confirmar que no hay escucha al enviar la aplicación a segundo plano. Verificar permisos y privacidad.
12. Probar `estudio automático` solo con contenido educativo propio y permiso explícito; confirmar que sigue bloqueado ante evaluaciones.

## Riesgos conocidos

- Una compilación exitosa no equivale a validación funcional en teléfono.
- APK GeckoView Nightly elevada en tamaño; rendimiento en gama media depende de memoria disponible y red.
- Descargas protegidas y contenido con credenciales necesitan implementación específica y consentimiento antes de habilitarse.
- `SpeechRecognizer` delega el reconocimiento al motor del sistema; velocidad, vocabulario y conectividad varían. El navegador no puede calibrar arbitrariamente la ganancia de micrófono.
- La firma debug de diferentes runners CI puede cambiar: Android podría requerir desinstalar la versión anterior para instalar una APK firmada con otra clave. **Desinstalar borra datos locales de ACCESSIA**.
