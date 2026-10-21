# ACCESSIA — Especificación de diseño (21 octubre 2026)

**Creado por:** **GENAI INDUSTRIES**, de **Luis Carlos Moreno Jiménez**, Santa Cruz de la Sierra, Bolivia. **Visibilidad:** pública; **licencia:** MIT; **coste para usuarios:** gratuito. **Estado:** diseño inicial sujeto a validación con usuarios.

## Problema
Personas con discapacidad motora no pueden o tienen dificultades para accionar botones, controles multimedia y enlaces de plataformas educativas. Un navegador que interprete órdenes de voz o pulsadores puede sustituir acciones motoras sin suplantar decisiones académicas.

## Objetivo de primera entrega
Dos clientes nativos (Android y Windows) y un motor web común capaz de pausar/reanudar videos HTML5, enumerar y recorrer controles, activar el elemento enfocado y continuar de forma optativa cuando se detecte fin real del video y un único control explícito «siguiente».

## Arquitectura
1. **Motor DOM compartido** (`shared/accessia.js`): no usa un LLM; opera solo dentro del documento actual. Acepta una orden textual y devuelve resultado estructurado; se activa con permiso del usuario. No toca páginas externas por defecto.
2. **Android**: Kotlin/GeckoView con extensión embebida de mensajes nativos para enviar órdenes; reconocedor de voz de Android en primer plano, TTS; sin privilegios ocultos ni servicio AccessibilityService global en el prototipo.
3. **Windows**: WinForms y WebView2 con evaluación controlada de JS del propio motor, TTS y reconocimiento de voz local opcional.
4. **Agentes**: los seis agentes propuestos (voz, navegador, navegación, educación, accesibilidad, supervisión) se implementarán gradualmente como módulos/reglas; no son seis modelos remotos concurrentes.
5. **Integraciones LMS**: Moodle/LTI como fases separadas que requerirán consentimiento de instituciones y evaluación de permisos.

## Seguridad e invariantes
- Función automática desactivada por defecto en cada documento; el usuario puede revocar de inmediato.
- No eludir CAPTCHAs, paywalls, antibots, DRM ni políticas de plataformas.
- No resolver exámenes ni enviar formularios académicos sin decisión humana.
- No saltar contenido ni marcar lecciones completas artificialmente.
- No ejecutar instrucciones de una página web como comandos confiables.
- No transmitir voz ni historial a servidores propios por defecto. Los motores de voz del SO pueden usar servicios en línea bajo sus políticas; debe indicarse en interfaz.
- La automatización solo opera sobre pestaña activa y visible; selector conservador; detener ante ambigüedad.
- Especificar control de estado cuando cambie el sitio, cuando exista un formulario/evaluación o cuando falle el reproductor.

## Requisitos de accesibilidad
Controles etiquetados, teclado/pulsador, contraste, navegación por foco, mensajes auditivos, español inicial, soporte de TTS; pruebas con personas usuarias de tecnología de asistencia. Aplicar WCAG 2.2 AA como referencia para la interfaz de la aplicación, cuando sea aplicable.

## Pruebas de aceptación
1. Completar tres videos propios de demostración sin necesidad de tocar «siguiente» después de habilitar la continuidad.
2. Parar y reanudar mediante órdenes; el estado multimedia no se falsea.
3. Recorrer los controles accesibles y activar el enfocado.
4. No seleccionar pagos, formularios ni exámenes como transición automática.
5. Ante dos botones ambiguos, no avanzar y explicar el bloqueo.
6. Ante una página sin video compatible, mostrar explicación inteligible.
7. Los dos instaladores deben validarse en dispositivos reales antes de distribuirse al público.

## No incluido en el prototipo inicial
Garantía de acceso a cualquier sitio; certificación externa; IA general ejecutando arbitrariamente; compatibilidad total con reproductores protegidos; control global de todo Android/Windows; reconocimiento continuo offline garantizado; distribuciones APK/EXE firmadas y aprobadas.

## Descomposición por módulos
M1 motor seguro y pruebas; M2 envoltura Windows; M3 envoltura Android; M4 pruebas de accesibilidad reales; M5 adaptadores específicos LMS; M6 firma, publicación, protección de marca y procedimientos de divulgación responsable.

## Revisión de diseño
Este documento recoge la aprobación conceptual dada por el solicitante el 21/10/2026; los criterios detallados y pruebas pendientes requieren revisión con usuarios y pruebas de dispositivos. Publicar como «prototipo», nunca como producto certificado.
