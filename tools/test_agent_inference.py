"""Optional real local-model smoke probe; never downloads or executes browser actions."""
import argparse
import hashlib
import json
from pathlib import Path
import time
import litert_lm

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--model', type=Path, required=True)
parser.add_argument('--report', type=Path, required=True)
args = parser.parse_args()
assert args.model.stat().st_size == 613722264, 'Use the pinned Android LiteRT model artifact'
with args.model.open('rb') as source:
    assert hashlib.file_digest(source, 'sha256').hexdigest() == 'c5f39b4191926786c3e6668b62af4d6eb15248dfc7a5348252a9b406f33f2108', 'Wrong model SHA256'
root = Path(__file__).resolve().parents[1]
class ProposalTool(litert_lm.Tool):
    def __init__(self, descriptor): self.descriptor = descriptor
    def get_tool_description(self): return {'type': 'function', 'function': self.descriptor}
    def execute(self, param): raise AssertionError('Automatic tool execution is forbidden')

tools = [ProposalTool(s) for s in json.loads((root / 'shared/agent-tools.json').read_text())]
observation = {'url': 'https://example.org/guia', 'title': 'Guía accesible', 'text': 'Esta guía explica navegación por voz y lectura de páginas.',
    'controls': [{'id': 'c1', 'name': 'Más información', 'role': 'a', 'href': 'https://example.org/mas'}],
    'sources': [{'id': 'page', 'url': 'https://example.org/guia', 'title': 'Guía accesible'}]}
cases = [('Encuentra información sobre accesibilidad en Bolivia', 'search'), ('Quisiera escuchar el contenido de esta página', 'read'),
    ('Abre el enlace de más información', 'open'), ('Muévete un poco hacia abajo', 'scroll_down')]
results = []
litert_lm.set_min_log_severity(litert_lm.LogSeverity.ERROR)
with litert_lm.Engine(model_path=str(args.model), backend=litert_lm.Backend.CPU(thread_count=4), max_num_tokens=4096) as engine:
    for request, expected in cases:
        start = time.monotonic()
        with engine.create_conversation(system_message=(root / 'shared/agent-prompt.txt').read_text(), tools=tools,
                automatic_tool_calling=False, extra_context={'enable_thinking': False}, max_output_tokens=256,
                sampler_config=litert_lm.SamplerConfig(top_k=1, temperature=0)) as chat:
            message = chat.send_message(json.dumps({'request': request, 'observation': observation}, ensure_ascii=False))
        calls = [{'name': c.name, 'arguments': c.arguments} for c in message.tool_calls]
        valid = len(calls) == 1 and calls[0]['name'] == expected
        if valid and expected == 'search':
            query = calls[0]['arguments'].get('value', '').lower()
            valid = 'accesibilidad' in query and 'bolivia' in query
        if valid and expected == 'open': valid = calls[0]['arguments'].get('target') == 'c1'
        result = {'request': request, 'calls': calls, 'passed': valid, 'seconds': round(time.monotonic() - start, 2)}
        print(json.dumps(result, ensure_ascii=False), flush=True)
        results.append(result)
args.report.write_text(json.dumps({'environment': 'Linux cloud CPU; no physical device or browser actions', 'cases': results}, ensure_ascii=False, indent=2))
raise SystemExit(0 if all(r['passed'] for r in results) else 1)
