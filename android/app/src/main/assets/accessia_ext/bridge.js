/* ACCESSIA bridge: only accepts commands from the native GeckoView session port. */
(function () {
  'use strict';
  if (!globalThis.Accessia || !browser?.runtime?.connectNative) return;
  const port = browser.runtime.connectNative('accessia');
  globalThis.Accessia.setFeedbackHandler(payload => port.postMessage({ type: 'result', payload }));
  port.onMessage.addListener(async (message) => {
    if (!message || message.type !== 'command' || typeof message.command !== 'string' || message.command.length > 140) return;
    try {
      const payload = await globalThis.Accessia.execute(message.command);
      port.postMessage({ type: 'result', payload });
    } catch (_error) {
      port.postMessage({ type: 'result', payload: { ok: false, message: 'Error al procesar el comando de accesibilidad.' } });
    }
  });
})();
