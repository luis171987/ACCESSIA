'use strict';
const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createEngine, parseCommand } = require('../accessia.js');

function element(tag, label, extra = {}) {
  const attrs = { ...(extra.attrs || {}) };
  return {
    tagName: tag.toUpperCase(), type: extra.type || '',
    disabled: !!extra.disabled, hidden: !!extra.hidden, inert: false,
    innerText: label, textContent: label, clicked: 0, focused: 0,
    getAttribute(k) { return Object.hasOwn(attrs, k) ? attrs[k] : null; },
    closest() { return null; },
    focus() { this.focused++; }, scrollIntoView() {}, click() { this.clicked++; },
    ...extra
  };
}
function documentFor({ controls = [], links = controls, videos = [], title = '', body = '', url = 'https://curso.example/leccion/1' } = {}) {
  const handlers = new Map();
  const doc = {
    title, URL: url, activeElement: null, body: { innerText: body },
    defaultView: { getComputedStyle() { return { display: 'block', visibility: 'visible' }; }, scrollBy() {} },
    querySelectorAll(selector) { return selector === 'video' ? videos : selector.startsWith('a[href],button,input') ? controls : links; },
    addEventListener(name, fn) { handlers.set(name, fn); }, removeEventListener(name) { handlers.delete(name); },
    triggerEnd(video) { handlers.get('ended')?.({ target: video }); }
  };
  controls.forEach(c => { c.focus = () => { c.focused++; doc.activeElement = c; }; });
  return doc;
}

test('reconoce acentos y comandos de voz', () => {
  assert.equal(parseCommand('  SIGUIENTE LECCIÓN  '), 'nextLesson');
  assert.equal(parseCommand('PAUSAR'), 'pause');
  assert.equal(parseCommand('desactivar estudio automático'), 'disableAuto');
});

test('foco siguiente y anterior y activación voluntaria', async () => {
  const a = element('button', 'Curso uno');
  const b = element('button', 'Curso dos');
  const engine = createEngine(documentFor({ controls: [a, b] }));
  assert.equal((await engine.execute('cursor siguiente')).index, 1);
  assert.equal((await engine.execute('cursor siguiente')).index, 2);
  assert.equal((await engine.execute('cursor anterior')).index, 1);
  assert.equal((await engine.execute('seleccionar')).ok, true);
  assert.equal(a.clicked, 1);
  assert.equal(b.clicked, 0);
});

test('ignora controles deshabilitados y ocultos', async () => {
  const a = element('button', 'Oculto', { hidden: true });
  const b = element('button', 'Deshabilitado', { disabled: true });
  const c = element('button', 'Activo');
  const engine = createEngine(documentFor({ controls: [a, b, c] }));
  assert.equal((await engine.execute('siguiente')).message.includes('Activo'), true);
});

test('pausa y reanuda video HTML5 sin saltarlo', async () => {
  const media = element('video', '', { paused: false, pause() { this.paused = true; }, async play() { this.paused = false; } });
  const engine = createEngine(documentFor({ videos: [media] }));
  assert.equal((await engine.execute('pausar')).ok, true);
  assert.equal(media.paused, true);
  assert.equal((await engine.execute('continuar')).ok, true);
  assert.equal(media.paused, false);
});

test('no activa continuidad sin video', async () => {
  const engine = createEngine(documentFor());
  assert.equal((await engine.execute('activar estudio automático')).ok, false);
  assert.equal(engine.isAutoEnabled(), false);
});

test('continuidad automática optativa y solo al terminar video', async () => {
  const next = element('a', 'Siguiente lección', { attrs: { href: '/leccion/2' } });
  const media = element('video', '', { ended: true, pause() {}, async play() {} });
  const doc = documentFor({ links: [next], videos: [media] });
  const engine = createEngine(doc);
  doc.triggerEnd(media);
  assert.equal(next.clicked, 0);
  assert.equal((await engine.execute('activar estudio automatico')).ok, true);
  doc.triggerEnd(media);
  assert.equal(next.clicked, 1);
});

test('impide continuidad ante examen', async () => {
  const next = element('button', 'Siguiente');
  const media = element('video', '', { ended: true, pause() {}, async play() {} });
  const doc = documentFor({ title: 'Evaluación final', links: [next], videos: [media] });
  const engine = createEngine(doc);
  await engine.execute('activar estudio automatico');
  doc.triggerEnd(media);
  assert.equal(next.clicked, 0);
});

test('bloquea siguientes ambiguos', async () => {
  const a = element('button', 'Siguiente');
  const b = element('button', 'Continuar');
  const engine = createEngine(documentFor({ links: [a, b] }));
  assert.equal((await engine.execute('siguiente lección')).ok, false);
  assert.equal(a.clicked + b.clicked, 0);
});

test('bloquea enlaces fuera de sitio y javascript URLs', async () => {
  const external = element('a', 'Siguiente', { attrs: { href: 'https://externo.invalid' } });
  const code = element('a', 'Siguiente', { attrs: { href: 'javascript:alert(1)' } });
  for (const link of [external, code]) {
    const engine = createEngine(documentFor({ links: [link] }));
    assert.equal((await engine.execute('siguiente lección')).ok, false);
    assert.equal(link.clicked, 0);
  }
});

test('desactivar y cerrar detiene navegación automática', async () => {
  const next = element('button', 'Siguiente');
  const media = element('video', '', { ended: true, pause() {}, async play() {} });
  const doc = documentFor({ links: [next], videos: [media] });
  const engine = createEngine(doc);
  await engine.execute('activar estudio automatico');
  await engine.execute('desactivar estudio automatico');
  doc.triggerEnd(media);
  assert.equal(next.clicked, 0);
  engine.dispose();
  assert.equal((await engine.execute('siguiente')).ok, false);
});


test('feedback informa cuando termina video y no se puede continuar', async () => {
  const media = element('video', '', { ended: true, pause() {}, async play() {} });
  const doc = documentFor({ videos: [media] });
  const received = [];
  const engine = createEngine(doc);
  engine.setFeedbackHandler(r => received.push(r.message));
  await engine.execute('activar estudio automatico');
  doc.triggerEnd(media);
  assert.equal(received.length, 1);
  assert.match(received[0], /No se encontró/);
});


test('ignora eventos ended sintéticos cuando el vídeo sigue en reproducción', async () => {
  const next = element('button', 'Siguiente');
  const media = element('video', '', { ended: false, pause() {}, async play() {} });
  const doc = documentFor({ links: [next], videos: [media] });
  const engine = createEngine(doc);
  await engine.execute('activar estudio automatico');
  doc.triggerEnd(media);
  assert.equal(next.clicked, 0);
});
