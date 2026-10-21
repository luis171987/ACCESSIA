"""In-browser smoke test with Playwright, using HTTPS requests fulfilled locally.
No TCP listener or external network is required. Run: python shared/tests/e2e_playwright.py
"""
import mimetypes
import shutil
from pathlib import Path
from urllib.parse import urlsplit
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
BASE = 'https://accessia.example'

def offline(route):
    resource = Path(urlsplit(route.request.url).path.lstrip('/'))
    target = (ROOT / resource).resolve()
    if not target.is_relative_to(ROOT) or not target.is_file():
        route.fulfill(status=404, body='Not found')
        return
    route.fulfill(status=200, body=target.read_bytes(), content_type=mimetypes.guess_type(target.name)[0] or 'application/octet-stream')

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True, executable_path=shutil.which('chromium'), args=['--no-sandbox', '--autoplay-policy=no-user-gesture-required'])
    try:
        page = browser.new_page()
        page.route(BASE + '/**', offline)
        page.goto(BASE + '/shared/demo/lesson1.html')
        assert page.locator('h1').inner_text().startswith('Lección 1')
        assert page.evaluate('window.Accessia !== undefined')
        assert not page.evaluate('window.Accessia.isAutoEnabled()')
        assert 'activada' in page.evaluate('window.Accessia.execute("activar estudio automático")').get('message', '')
        page.locator('video').evaluate('v => v.play()')
        page.wait_for_url('**/lesson2.html', timeout=10000)
        assert 'Lección 2' in page.locator('h1').inner_text()
        assert not page.evaluate('window.Accessia.isAutoEnabled()')
        print('E2E PASS: permiso → evento ended → lección 2 → desactivar permiso en documento nuevo')
    finally:
        browser.close()
