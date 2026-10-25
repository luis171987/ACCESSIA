"""Real DOM regression tests; fulfilled test pages, no network or physical device."""
from pathlib import Path
import os
import unittest
from playwright.sync_api import sync_playwright


class AgentDomTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.playwright = sync_playwright().start()
        options = {"headless": True, "args": ["--no-sandbox"]}
        if os.environ.get("ACCESSIA_CHROMIUM_PATH"):
            options["executable_path"] = os.environ["ACCESSIA_CHROMIUM_PATH"]
        cls.browser = cls.playwright.chromium.launch(**options)

    @classmethod
    def tearDownClass(cls):
        cls.browser.close()
        cls.playwright.stop()

    def setUp(self):
        self.page = self.browser.new_page()
        self.page.route("**/*", lambda route: route.fulfill(status=200, content_type="text/html", body="<html><body></body></html>"))
        self.page.goto("https://sandbox.accessia.test/")

    def tearDown(self):
        self.page.close()

    def load(self, html):
        self.page.set_content(html)
        self.page.add_script_tag(content=(Path(__file__).resolve().parents[1] / "shared/accessia.js").read_text())

    def test_wrapped_labels_and_editable_text_are_private(self):
        self.load('<label>Notas privadas<textarea>SECRET_WRAPPED</textarea></label><div contenteditable="true" role="textbox">SECRET_DRAFT</div><p>Texto público.</p>')
        observation = self.page.evaluate("Accessia.agentObserve()")
        self.assertNotIn("SECRET_", str(observation))
        self.assertEqual(observation["controls"][0]["name"], "Notas privadas")
        self.assertIn("Texto público", observation["text"])

    def test_form_destination_mutation_rejects_confirmation(self):
        self.load('<form action="/preview"><button type="button">Continuar</button></form>')
        observation = self.page.evaluate("Accessia.agentObserve()")
        self.page.evaluate("document.querySelector('button').type='submit';document.querySelector('form').action='/send'")
        result = self.page.evaluate("p=>Accessia.agentExecute(p,true)", {"action": "click", "snapshot": observation["snapshot"], "target": "c1"})
        self.assertFalse(result["ok"])

    def test_fill_uses_native_setter_and_emits_input_after_confirmation(self):
        self.load('<label>Buscar<input aria-label="Buscar"></label><script>window.inputs=0;document.querySelector("input").addEventListener("input",()=>window.inputs++)</script>')
        observation = self.page.evaluate("Accessia.agentObserve()")
        proposal = {"action": "fill", "snapshot": observation["snapshot"], "target": "c1", "value": "Accesibilidad"}
        self.assertTrue(self.page.evaluate("p=>Accessia.agentExecute(p)", proposal)["requiresConfirmation"])
        self.assertEqual(self.page.locator("input").input_value(), "")
        self.assertTrue(self.page.evaluate("p=>Accessia.agentExecute(p,true)", proposal)["ok"])
        self.assertEqual(self.page.locator("input").input_value(), "Accesibilidad")
        self.assertEqual(self.page.evaluate("window.inputs"), 1)

    def test_click_requires_confirmation_and_cannot_be_replayed(self):
        self.load('<button type="button" onclick="window.clicks=(window.clicks||0)+1">Abrir panel</button>')
        observation = self.page.evaluate("Accessia.agentObserve()")
        proposal = {"action": "click", "snapshot": observation["snapshot"], "target": "c1", "confirmed": True}
        self.assertTrue(self.page.evaluate("p=>Accessia.agentExecute(p)", proposal)["requiresConfirmation"])
        self.assertIsNone(self.page.evaluate("window.clicks"))
        self.assertTrue(self.page.evaluate("p=>Accessia.agentExecute(p,true)", proposal)["ok"])
        self.assertFalse(self.page.evaluate("p=>Accessia.agentExecute(p,true)", proposal)["ok"])
        self.assertEqual(self.page.evaluate("window.clicks"), 1)


if __name__ == "__main__":
    unittest.main()
