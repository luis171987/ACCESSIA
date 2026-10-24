/* ACCESSIA shared accessibility engine. MIT License. No external dependencies. */
(function (global) {
  "use strict";

  const FOCUS_SELECTOR = 'a[href],button,input:not([type="hidden"]),select,textarea,[role="button"],[role="link"],[tabindex]';
  const NEXT_SELECTOR = 'a[href],button,[role="button"],[role="link"]';
  const NEXT_TEXT = /^(?:(?:ir a |ver )?(?:la )?(?:siguiente|proxima|próxima|next)(?: (?:leccion|lección|clase|video|vídeo|unidad|lesson))?|(?:continuar|continue|continue course|next lesson|siguiente leccion|siguiente lección))\s*[›»→>]?$|^[›»→>]$/i;
  const QUIZ_WORDS = /\b(?:examen|evaluacion|evaluación|cuestionario|quiz|assessment|submit quiz|enviar respuestas|enviar examen|entregar tarea)\b/i;

  function fold(value) {
    return String(value == null ? '' : value).normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '').toLowerCase().replace(/\s+/g, ' ').trim();
  }

  function parseCommand(input) {
    const s = fold(input);
    if (/^(pausar|pausa|detener video|detener video|pause)$/.test(s)) return 'pause';
    if (/^(continuar|reanudar|reproducir|play|resume|seguir video)$/.test(s)) return 'play';
    if (/^(cursor siguiente|siguiente control|siguiente opcion|siguiente|next control)$/.test(s)) return 'focusNext';
    if (/^(cursor anterior|cursor volver|anterior|control anterior)$/.test(s)) return 'focusPrevious';
    if (/^(seleccionar|activar|hacer clic|click|abrir seleccionado)$/.test(s)) return 'activate';
    if (/^(bajar|desplazar abajo|scroll down)$/.test(s)) return 'scrollDown';
    if (/^(subir|desplazar arriba|scroll up)$/.test(s)) return 'scrollUp';
    if (/^(siguiente leccion|siguiente clase|proximo video|proxima leccion)$/.test(s)) return 'nextLesson';
    if (/^(activar estudio automatico|estudio automatico activar|activar continuidad)$/.test(s)) return 'enableAuto';
    if (/^(desactivar estudio automatico|detener automatizacion|parar automatizacion|desactivar continuidad)$/.test(s)) return 'disableAuto';
    if (/^(leer pagina|leer esta pagina|leer contenido)$/.test(s)) return 'readPage';
    if (/^(estado|ayuda)$/.test(s)) return 'status';
    return 'unknown';
  }

  function createEngine(doc, options) {
    if (!doc || typeof doc.querySelectorAll !== 'function') throw new TypeError('document requerido');
    const settings = options || {};
    let auto = false;
    let focusIndex = -1;
    let disposed = false;
    let lastAutoAt = 0;
    let feedback = typeof settings.onFeedback === 'function' ? settings.onFeedback : null;
    let observation = null;
    let observationSequence = 0;

    function result(ok, message, extra) {
      return Object.assign({ ok: Boolean(ok), message: String(message) }, extra || {});
    }

    function visible(el) {
      if (!el || el.disabled || el.hidden || el.inert || el.getAttribute('aria-hidden') === 'true') return false;
      if (typeof el.closest === 'function' && el.closest('[hidden],[inert],[aria-hidden="true"]')) return false;
      const view = doc.defaultView;
      if (view && typeof view.getComputedStyle === 'function') {
        const style = view.getComputedStyle(el);
        if (style && (style.display === 'none' || style.visibility === 'hidden')) return false;
      }
      return true;
    }

    function getAccessibleName(el) {
      return String(el.getAttribute('aria-label') || el.getAttribute('title') || el.innerText || el.textContent || '').trim();
    }

    function controls() {
      return Array.from(doc.querySelectorAll(FOCUS_SELECTOR)).filter(el =>
        visible(el) && el.getAttribute('tabindex') !== '-1' && el.getAttribute('aria-disabled') !== 'true'
      );
    }

    function focus(direction) {
      const items = controls();
      if (!items.length) return result(false, 'No se encontraron controles accesibles.');
      const active = items.indexOf(doc.activeElement);
      focusIndex = active >= 0 ? active : focusIndex;
      focusIndex = (focusIndex + direction + items.length) % items.length;
      const chosen = items[focusIndex];
      if (typeof chosen.focus === 'function') chosen.focus();
      if (typeof chosen.scrollIntoView === 'function') chosen.scrollIntoView({ block: 'nearest' });
      return result(true, `Control ${focusIndex + 1} de ${items.length}: ${getAccessibleName(chosen) || chosen.tagName.toLowerCase()}`, { index: focusIndex + 1 });
    }

    function selectedAction() {
      const el = doc.activeElement;
      if (!el || !visible(el) || !controls().includes(el)) return result(false, 'Primero selecciona un control.');
      if (el.getAttribute('aria-disabled') === 'true') return result(false, 'Este control está deshabilitado.');
      if (typeof el.click === 'function') el.click();
      return result(true, 'Control activado por orden de la persona.');
    }

    function video() {
      return Array.from(doc.querySelectorAll('video')).find(visible) || null;
    }

    async function videoCommand(action) {
      const el = video();
      if (!el) return result(false, 'Esta página no contiene un video HTML5 compatible.');
      if (action === 'pause') {
        if (typeof el.pause === 'function') el.pause();
        return result(true, 'Video pausado.');
      }
      try {
        if (typeof el.play !== 'function') return result(false, 'El reproductor no admite esta operación.');
        await el.play();
        return result(true, 'Reproduciendo video.');
      } catch (_error) {
        return result(false, 'El reproductor requiere una acción directa o no permite iniciar automáticamente.');
      }
    }

    function findNext() {
      // Conservative guard: avoid submitting/advancing assessments or interactive forms.
      const pageText = String(doc.body && (doc.body.innerText || doc.body.textContent) || '').slice(0, 12000);
      const title = String(doc.title || '');
      if (QUIZ_WORDS.test(title) || QUIZ_WORDS.test(pageText)) {
        return result(false, 'Se detectó una posible evaluación; requiere decisión humana.');
      }
      const candidates = Array.from(doc.querySelectorAll(NEXT_SELECTOR)).filter(el => {
        if (!visible(el) || el.getAttribute('aria-disabled') === 'true') return false;
        if (el.tagName && el.tagName.toUpperCase() === 'BUTTON' && String(el.type || '').toLowerCase() === 'submit') return false;
        const raw = getAccessibleName(el);
        return Boolean(raw && NEXT_TEXT.test(raw));
      });
      if (candidates.length !== 1) {
        return result(false, candidates.length ? 'Hay varias opciones para continuar; se necesita una selección.' : 'No se encontró un único control explícito de siguiente lección.');
      }
      const next = candidates[0];
      // Do not auto-follow external origins or javascript: URLs.
      if (next.getAttribute && next.getAttribute('href')) {
        const rawHref = next.getAttribute('href');
        try {
          const here = new URL(doc.URL || 'https://local.invalid/');
          const destination = new URL(rawHref, here);
          if (!['https:', 'http:'].includes(destination.protocol) || destination.origin !== here.origin) {
            return result(false, 'El enlace sale del sitio actual o usa un protocolo no admitido.');
          }
        } catch (_error) { return result(false, 'Enlace de continuación no válido.'); }
      }
      return result(true, 'Se encontró una continuación autorizada.', { element: next });
    }

    function nextLesson(automatic) {
      const candidate = findNext();
      if (!candidate.ok) return candidate;
      if (automatic && !auto) return result(false, 'La continuidad automática está desactivada.');
      const el = candidate.element;
      if (typeof el.click === 'function') el.click();
      return result(true, automatic ? 'Pasando a la siguiente lección tras finalizar el video.' : 'Se activó la siguiente lección por orden del usuario.');
    }

    function mediaEnded(event) {
      if (disposed || !auto || !event || !event.target || event.target.tagName !== 'VIDEO') return;
      if (!visible(event.target) || event.target !== video() || event.target.ended !== true) return;
      const now = Date.now();
      if (now - lastAutoAt < 2500) return; // Debounce ended events.
      lastAutoAt = now;
      const outcome = nextLesson(true);
      if (typeof feedback === 'function') feedback(outcome);
    }
    if (typeof doc.addEventListener === 'function') doc.addEventListener('ended', mediaEnded, true);

    // AI never receives executable code, selectors or private field values.
    function httpsUrl(value) {
      try {
        const u = new URL(value, doc.URL);
        return u.protocol === 'https:' && !u.username && !u.password ? u.href : '';
      } catch (_) { return ''; }
    }

    function privateField(el) {
      const type = fold(el.type || el.getAttribute('type'));
      const autocomplete = fold(el.getAttribute('autocomplete'));
      return ['password', 'file', 'hidden'].includes(type) ||
        /(?:^|\s)(?:cc-\S+|current-password|new-password|one-time-code)(?:\s|$)/.test(autocomplete);
    }

    function agentItems() {
      return controls().filter(el => !privateField(el) &&
        (typeof el.getClientRects !== 'function' || el.getClientRects().length > 0)).slice(0, 40);
    }

    function describe(el, index) {
      const editable = ['INPUT', 'TEXTAREA', 'SELECT'].includes(String(el.tagName).toUpperCase()) ||
        el.isContentEditable || el.getAttribute('contenteditable') === 'true' || el.getAttribute('role') === 'textbox';
      const name = editable ? String(el.getAttribute('aria-label') || el.getAttribute('title') ||
        Array.from(el.labels || []).map(label => readableText(label, 160)).join(' ') || el.getAttribute('placeholder') || '') : getAccessibleName(el);
      return { id: `c${index + 1}`, name: name.slice(0, 160),
        role: String(el.getAttribute('role') || el.tagName || '').toLowerCase().slice(0, 30),
        href: el.getAttribute('href') ? httpsUrl(el.getAttribute('href')) : '' };
    }

    function readableText(root, limit) {
      if (root && typeof doc.createTreeWalker === 'function') {
        const walker = doc.createTreeWalker(root, 4); // SHOW_TEXT
        const parts = []; let length = 0; let node;
        while (length < limit && (node = walker.nextNode())) {
          const parent = node.parentElement;
          if (!parent || !visible(parent) || parent.closest('input,textarea,select,option,[contenteditable]:not([contenteditable="false"]),[role="textbox"],script,style,noscript')) continue;
          if (typeof parent.getClientRects === 'function' && parent.getClientRects().length === 0) continue;
          const value = String(node.textContent || '').replace(/\s+/g, ' ').trim();
          if (value) { parts.push(value); length += value.length + 1; }
        }
        return parts.join(' ').slice(0, limit);
      }
      return String(root && (root.innerText || root.textContent) || '').trim().slice(0, limit);
    }

    function pageText() {
      return readableText(doc.body, 4500);
    }

    function actionFingerprint(el) {
      return JSON.stringify([el.type || '', el.formAction || '', el.formMethod || '', el.formTarget || '',
        el.form && el.form.action || '', el.form && el.form.method || '', el.form && el.form.target || '',
        el.getAttribute('form'), el.getAttribute('onclick'), el.getAttribute('contenteditable')]);
    }

    function agentObserve() {
      if (disposed) return result(false, 'Motor cerrado.');
      const items = agentItems();
      const descriptions = items.map(describe);
      const url = String(doc.URL || '');
      const title = String(doc.title || '').slice(0, 200);
      const text = pageText();
      const snapshot = `a${++observationSequence}-${Date.now()}`;
      const sources = httpsUrl(url) ? [{ id: 'page', url: httpsUrl(url), title }] : [];
      observation = { snapshot, url, title, text, items, descriptions, fingerprints: items.map(actionFingerprint), sources, at: Date.now() };
      return { ok: true, snapshot, url, title, text, controls: descriptions, sources };
    }

    function currentObservation(snapshot) {
      const s = observation;
      if (!s || disposed || snapshot !== s.snapshot || Date.now() - s.at > 120000 ||
          String(doc.URL || '') !== s.url || String(doc.title || '').slice(0, 200) !== s.title || pageText() !== s.text) return false;
      const items = agentItems();
      return items.length === s.items.length && items.every((el, i) => el === s.items[i] &&
        JSON.stringify(describe(el, i)) === JSON.stringify(s.descriptions[i]) && actionFingerprint(el) === s.fingerprints[i]);
    }

    function agentExecute(proposal, confirmed = false) {
      if (!proposal || typeof proposal !== 'object' || Array.isArray(proposal) || !currentObservation(proposal.snapshot))
        return result(false, 'La página cambió o la observación caducó. Repite tu petición.');
      const action = proposal.action;
      if (!['read', 'scroll_up', 'scroll_down', 'focus', 'click', 'fill', 'search', 'open', 'answer', 'clarify'].includes(action))
        return result(false, 'La IA propuso una herramienta no permitida.');
      if (QUIZ_WORDS.test(observation.title) || QUIZ_WORDS.test(observation.text))
        return result(false, 'Posible evaluación: usa los controles manuales; el asistente no decide respuestas.');
      if (action === 'read') return result(Boolean(observation.text), observation.text ? 'Texto observado de la página.' : 'No hay texto visible.',
        { text: observation.text.slice(0, 1800), sources: observation.sources });
      if (action === 'clarify') return result(false, 'Necesito una aclaración.',
        { text: typeof proposal.text === 'string' ? proposal.text.slice(0, 500) : 'Describe qué deseas hacer.' });
      if (action === 'answer') {
        const citations = proposal.citations;
        if (typeof proposal.text !== 'string' || !proposal.text.trim() || !Array.isArray(citations) ||
            !citations.length || citations.length > 6 || !observation.sources.length || citations.some(id => id !== 'page'))
          return result(false, 'La respuesta no aporta una fuente consultada válida.');
        return result(true, 'Resumen generado localmente; comprueba la fuente, puede contener errores.',
          { text: proposal.text.slice(0, 1000) + '\nFuente consultada: ' + observation.sources[0].url +
              '\nResumen generado por IA; puede contener errores.', sources: observation.sources });
      }
      if (action === 'scroll_up' || action === 'scroll_down') {
        const w = doc.defaultView;
        if (!w || typeof w.scrollBy !== 'function') return result(false, 'Desplazamiento no disponible.');
        w.scrollBy({ top: action === 'scroll_up' ? -500 : 500, behavior: 'smooth' });
        return result(true, 'Desplazamiento solicitado.');
      }
      const index = observation.descriptions.findIndex(c => c.id === proposal.target);
      const el = index >= 0 ? observation.items[index] : null;
      if (['focus', 'click', 'fill'].includes(action) && !el) return result(false, 'El control no figura en la observación.');
      if (action === 'focus') {
        if (typeof el.focus === 'function') el.focus();
        return result(doc.activeElement === el, doc.activeElement === el ? 'Foco comprobado en el control.' : 'No se pudo comprobar el foco.');
      }
      const value = typeof proposal.value === 'string' ? proposal.value.trim() : '';
      let destination = '';
      if (action === 'search') {
        if (!value || value.length > 500) return result(false, 'Consulta vacía o demasiado extensa.');
        destination = 'https://www.google.com/search?q=' + encodeURIComponent(value);
      }
      if (action === 'open') {
        destination = el ? observation.descriptions[index].href : httpsUrl(value);
        const allowed = [httpsUrl(observation.url), ...observation.descriptions.map(c => c.href)].filter(Boolean);
        if (!destination || !allowed.includes(destination)) return result(false, 'El destino no es un enlace HTTPS observado.');
      }
      if (action === 'click' && el.getAttribute('href') && !httpsUrl(el.getAttribute('href')))
        return result(false, 'Este enlace utiliza un destino no permitido.');
      if (action === 'fill' && (!['INPUT', 'TEXTAREA'].includes(String(el.tagName).toUpperCase()) || privateField(el) ||
          (String(el.tagName).toUpperCase() === 'INPUT' && !['', 'text', 'search', 'email', 'url', 'tel'].includes(fold(el.type))) ||
          typeof proposal.value !== 'string' || proposal.value.length > 1000))
        return result(false, 'No se permite escribir en este campo o el texto es demasiado extenso.');
      const preview = destination || (action === 'fill' ? `${getAccessibleName(el)}: ${proposal.value}` : getAccessibleName(el));
      if (confirmed !== true) return result(false, `¿Confirmas ${action}: ${preview.slice(0, 650)}?`, { requiresConfirmation: true });
      // Each accepted effect consumes the observation: confirmations cannot be replayed.
      observation = null;
      if (destination) return result(true, 'Navegación autorizada; resultado de carga pendiente.', { navigation: { url: destination } });
      if (action === 'click') {
        if (typeof el.click !== 'function') return result(false, 'El control no admite clic.');
        el.click();
        return result(true, 'Clic enviado al control; comprueba el resultado antes de continuar.');
      }
      if (action === 'fill') {
        const view = doc.defaultView;
        const proto = view && (String(el.tagName).toUpperCase() === 'INPUT' ? view.HTMLInputElement : view.HTMLTextAreaElement);
        const setter = proto && Object.getOwnPropertyDescriptor(proto.prototype, 'value')?.set;
        if (setter) setter.call(el, proposal.value); else el.value = proposal.value;
        if (view && typeof view.Event === 'function' && typeof el.dispatchEvent === 'function') {
          el.dispatchEvent(new view.Event('input', { bubbles: true }));
          el.dispatchEvent(new view.Event('change', { bubbles: true }));
        }
        return result(el.value === proposal.value, el.value === proposal.value ? 'Texto comprobado en el campo; no se ha enviado el formulario.' : 'El campo no conserva el texto solicitado.');
      }
      return result(false, 'Acción no disponible.');
    }

    async function execute(raw) {
      if (disposed) return result(false, 'Motor de accesibilidad cerrado.');
      const action = parseCommand(raw);
      switch (action) {
        case 'focusNext': return focus(1);
        case 'focusPrevious': return focus(-1);
        case 'activate': return selectedAction();
        case 'pause': return videoCommand('pause');
        case 'play': return videoCommand('play');
        case 'enableAuto':
          if (!video()) return result(false, 'No hay video HTML5 compatible; no se activó la continuidad.');
          auto = true;
          return result(true, 'Continuidad activada solo para esta página. Se detendrá ante evaluaciones o ambigüedad.');
        case 'disableAuto': auto = false; return result(true, 'Continuidad automática desactivada.');
        case 'nextLesson': return nextLesson(false);
        case 'scrollDown':
        case 'scrollUp': {
          const w = doc.defaultView;
          if (!w || typeof w.scrollBy !== 'function') return result(false, 'Desplazamiento no disponible.');
          w.scrollBy({ top: action === 'scrollDown' ? 500 : -500, behavior: 'smooth' });
          return result(true, action === 'scrollDown' ? 'Desplazando hacia abajo.' : 'Desplazando hacia arriba.');
        }
        case 'readPage': {
          const content = String((doc.body && (doc.body.innerText || doc.body.textContent)) || '').trim();
          return result(Boolean(content), content ? 'Texto principal extraído.' : 'No se encontró contenido para leer.', { text: content.slice(0, 1800) });
        }
        case 'status': return result(true, `ACCESSIA activo. Continuidad ${auto ? 'activada' : 'desactivada'}.`);
        default: return result(false, 'Comando no reconocido. Di «ayuda» o selecciona una opción.');
      }
    }

    function dispose() {
      auto = false;
      observation = null;
      disposed = true;
      if (typeof doc.removeEventListener === 'function') doc.removeEventListener('ended', mediaEnded, true);
    }

    return { execute, agentObserve, agentExecute, dispose, isAutoEnabled: () => auto, parseCommand,
      setFeedbackHandler(handler) { feedback = typeof handler === 'function' ? handler : null; } };
  }

  const exportsValue = { createEngine, parseCommand };
  if (typeof module !== 'undefined' && module.exports) module.exports = exportsValue;
  if (global && global.document) global.Accessia = createEngine(global.document);
})(typeof globalThis !== 'undefined' ? globalThis : this);
