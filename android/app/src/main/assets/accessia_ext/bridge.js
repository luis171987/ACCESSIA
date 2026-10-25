/* ACCESSIA bridge: only accepts commands from the native GeckoView session port. */
(function () {
  'use strict';
  if (!globalThis.Accessia || !browser?.runtime?.connectNative) return;
  const port = browser.runtime.connectNative('accessia');
  globalThis.Accessia.setFeedbackHandler(payload => port.postMessage({ type: 'result', payload }));
  port.onMessage.addListener(async (message) => {
    if (message && ['agent_observe', 'agent_execute'].includes(message.type) && typeof message.id === 'string' && message.id.length <= 80) {
      try {
        const payload = message.type === 'agent_observe' ? await globalThis.Accessia.agentObserve() : await globalThis.Accessia.agentExecute(message.proposal, message.confirmed === true);
        port.postMessage({type: 'agent_result', id: message.id, payload});
      } catch (_error) { port.postMessage({type: 'agent_result', id: message.id, payload: {ok:false, message:'Error del asistente en esta página.'}}); }
      return;
    }
    if (!message || message.type !== 'command' || typeof message.command !== 'string' || message.command.length > 140) return;
    try {
      const payload = await globalThis.Accessia.execute(message.command);
      port.postMessage({ type: 'result', payload });
    } catch (_error) {
      port.postMessage({ type: 'result', payload: { ok: false, message: 'Error al procesar el comando de accesibilidad.' } });
    }
  });
})();
