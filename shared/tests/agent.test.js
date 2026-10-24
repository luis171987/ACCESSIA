'use strict';
const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createEngine } = require('../accessia.js');

function fixture() {
  const controls = [];
  const doc = { URL: 'https://example.org/article', title: 'Guía accesible', body: { innerText: 'Contenido de la guía.' }, activeElement: null,
    querySelectorAll: () => controls, addEventListener() {}, removeEventListener() {},
    defaultView: { getComputedStyle: () => ({ display: 'block', visibility: 'visible' }), scrollBy() {} } };
  function add(tag, name, attrs = {}) {
    const el = { tagName: tag.toUpperCase(), innerText: name, textContent: name, value: '', type: attrs.type || '',
      getAttribute: k => attrs[k] ?? null, closest: () => null, focus() { doc.activeElement = el; }, scrollIntoView() {},
      click() { el.clicked = (el.clicked || 0) + 1; }, dispatchEvent() {}, ...attrs };
    controls.push(el); return el;
  }
  return { doc, controls, add, engine: createEngine(doc) };
}

test('observa controles visibles sin contraseñas, valores ni URLs inseguras', () => {
  const f = fixture(); f.add('input', 'Clave', { type: 'password', value: 'SECRETO' });
  f.add('button', 'Invisible', { hidden: true });
  f.add('a', 'Guía', { href: '/guia' }); f.add('a', 'Código', { href: 'javascript:alert(1)' });
  const s = f.engine.agentObserve();
  assert.equal(s.ok, true); assert.ok(s.snapshot);
  assert.equal(s.controls.some(c => c.name === 'Clave' || c.name === 'Invisible'), false);
  assert.equal(JSON.stringify(s).includes('SECRETO'), false);
  assert.equal(s.controls.find(c => c.name === 'Guía').href, 'https://example.org/guia');
  assert.equal(s.controls.find(c => c.name === 'Código').href, '');
  assert.equal(s.sources[0].url, f.doc.URL);
});

test('acota observación, número de controles y texto de página', () => {
  const f = fixture(); for (let i = 0; i < 60; i++) f.add('button', `Botón ${i}`);
  f.doc.body.innerText = 'x'.repeat(10000);
  const s = f.engine.agentObserve(); assert.equal(s.controls.length, 40); assert.equal(s.text.length, 4500);
});

test('no ejecuta clic aunque el modelo incluya confirmed=true', async () => {
  const f = fixture(); const b = f.add('button', 'Guardar', { type: 'button' }); const s = f.engine.agentObserve();
  const p = { action: 'click', snapshot: s.snapshot, target: s.controls[0].id, confirmed: true };
  const r = await f.engine.agentExecute(p); assert.equal(r.requiresConfirmation, true); assert.equal(b.clicked || 0, 0);
  const done = await f.engine.agentExecute(p, true); assert.equal(done.ok, true); assert.equal(b.clicked, 1);
  assert.match(done.message, /envi|resultado/i);
});

test('rechaza instantáneas sustituidas, navegación y cambios de control', async () => {
  const f = fixture(); const b = f.add('button', 'Guardar'); const old = f.engine.agentObserve();
  const s = f.engine.agentObserve(); assert.equal((await f.engine.agentExecute({ action: 'read', snapshot: old.snapshot })).ok, false);
  b.innerText = b.textContent = 'Comprar';
  assert.equal((await f.engine.agentExecute({ action: 'click', snapshot: s.snapshot, target: s.controls[0].id }, true)).ok, false);
  const latest = f.engine.agentObserve(); f.doc.URL = 'https://example.org/otra';
  assert.equal((await f.engine.agentExecute({ action: 'read', snapshot: latest.snapshot })).ok, false);
});

test('escritura requiere confirmación y no permite campos privados ni archivos', async () => {
  const f = fixture(); const input = f.add('input', 'Buscar', { type: 'text' }); const s = f.engine.agentObserve();
  const p = { action: 'fill', snapshot: s.snapshot, target: s.controls[0].id, value: 'Accesibilidad' };
  assert.equal((await f.engine.agentExecute(p)).requiresConfirmation, true); assert.equal(input.value, '');
  assert.equal((await f.engine.agentExecute(p, true)).ok, true); assert.equal(input.value, 'Accesibilidad');
  const f2 = fixture(); f2.add('input', 'Tarjeta', { type: 'text', autocomplete: 'cc-number' });
  assert.equal(f2.engine.agentObserve().controls.length, 0);
});

test('búsqueda devuelve navegación solo tras consentimiento y codifica consulta', async () => {
  const f = fixture(); const s = f.engine.agentObserve();
  const p = { action: 'search', snapshot: s.snapshot, value: 'educación & inclusión' };
  const r = await f.engine.agentExecute(p); assert.equal(r.requiresConfirmation, true); assert.equal(r.navigation, undefined);
  const yes = await f.engine.agentExecute(p, true);
  assert.equal(yes.navigation.url, 'https://www.google.com/search?q=educaci%C3%B3n%20%26%20inclusi%C3%B3n');
});

test('abrir solo admite enlaces observados HTTPS sin credenciales', async () => {
  const f = fixture(); f.add('a', 'Guía', { href: '/guia' }); let s = f.engine.agentObserve();
  assert.equal((await f.engine.agentExecute({ action: 'open', snapshot: s.snapshot, value: 'https://inventado.invalid' }, true)).ok, false);
  assert.equal((await f.engine.agentExecute({ action: 'open', snapshot: s.snapshot, target: s.controls[0].id }, true)).navigation.url, 'https://example.org/guia');
  for (const url of ['javascript:alert(1)', 'file:///tmp/a', 'http://example.org', 'https://user:pass@example.org']) {
    const g = fixture(); g.add('a', 'Enlace', { href: url }); s = g.engine.agentObserve();
    assert.equal((await g.engine.agentExecute({ action: 'open', snapshot: s.snapshot, target: s.controls[0].id }, true)).ok, false);
  }
});

test('respuestas requieren citas observadas y muestran URL de respaldo', async () => {
  const f = fixture(); const s = f.engine.agentObserve();
  for (const citations of [[], ['inventada'], ['https://inventada.invalid']])
    assert.equal((await f.engine.agentExecute({ action: 'answer', snapshot: s.snapshot, text: 'Resumen', citations })).ok, false);
  const r = await f.engine.agentExecute({ action: 'answer', snapshot: s.snapshot, text: 'Resumen', citations: ['page'] });
  assert.equal(r.ok, true); assert.equal(r.sources[0].url, f.doc.URL); assert.match(r.text, /https:\/\/example.org/);
});

test('no realiza acciones IA sobre evaluaciones y rechaza herramientas desconocidas', async () => {
  const f = fixture(); f.add('button', 'Enviar respuestas'); f.doc.title = 'Evaluación final'; const s = f.engine.agentObserve();
  assert.equal((await f.engine.agentExecute({ action: 'click', snapshot: s.snapshot, target: s.controls[0].id }, true)).ok, false);
  assert.equal((await f.engine.agentExecute({ action: 'eval', snapshot: s.snapshot, value: 'alert(1)' }, true)).ok, false);
});

test('caducidad y cierre invalidan acciones y foco comprueba efecto observado', async () => {
  const f = fixture(); const b = f.add('button', 'Leer'); let s = f.engine.agentObserve();
  assert.equal((await f.engine.agentExecute({ action: 'focus', snapshot: s.snapshot, target: s.controls[0].id })).ok, true);
  assert.equal(f.doc.activeElement, b);
  s = f.engine.agentObserve(); const now = Date.now;
  try { Date.now = () => now() + 121000; assert.equal((await f.engine.agentExecute({ action: 'read', snapshot: s.snapshot })).ok, false); }
  finally { Date.now = now; }
  f.engine.dispose(); assert.equal(f.engine.agentObserve().ok, false);
});

test('nombres de campos no exponen texto de textarea o contenido editable', () => {
  const f = fixture(); f.add('textarea', 'NOTA_MEDICA_PRIVADA');
  f.add('div', 'BORRADOR_PRIVADO', { contenteditable: 'true', role: 'textbox' });
  const s = f.engine.agentObserve();
  assert.equal(JSON.stringify(s.controls).includes('NOTA_MEDICA_PRIVADA'), false);
  assert.equal(JSON.stringify(s.controls).includes('BORRADOR_PRIVADO'), false);
});

test('cambio de tipo y destino del formulario invalida una confirmación', async () => {
  const f = fixture(); const form = { action: 'https://example.org/preview', method: 'get', target: '' };
  const button = f.add('button', 'Continuar', { type: 'button', form }); const s = f.engine.agentObserve();
  button.type = 'submit'; form.action = 'https://example.org/send'; form.method = 'post';
  const r = await f.engine.agentExecute({ action: 'click', snapshot: s.snapshot, target: s.controls[0].id }, true);
  assert.equal(r.ok, false); assert.equal(button.clicked || 0, 0);
});

test('una confirmación consumida no puede repetirse', async () => {
  const f = fixture(); const b = f.add('button', 'Guardar'); const s = f.engine.agentObserve();
  const p = { action: 'click', snapshot: s.snapshot, target: s.controls[0].id };
  assert.equal((await f.engine.agentExecute(p, true)).ok, true);
  assert.equal((await f.engine.agentExecute(p, true)).ok, false); assert.equal(b.clicked, 1);
});
