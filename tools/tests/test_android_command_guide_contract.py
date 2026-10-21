from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[2]
ACTIVITY = ROOT / "android/app/src/main/java/org/accessia/browser/MainActivity.kt"
DOC = ROOT / "docs/GUIA_COMANDOS_ACCESSIA.md"
README = ROOT / "README.md"


class AccessiaCommandGuideContract(unittest.TestCase):
    def test_help_available_offline_and_in_the_launcher_screen(self):
        source = ACTIVITY.read_text()
        self.assertIn('private fun showCommandGuide()', source)
        self.assertIn('actionButton("Ayuda") { showCommandGuide() }', source)
        self.assertIn('actionButton("📖 Guía de comandos") { showCommandGuide() }', source)
        self.assertIn('"about:blank"', source)
        self.assertIn('"mostrar comandos"', source)
        self.assertIn('"que puedo decir"', source)

    def test_local_browser_actions_prioritize_extension(self):
        source = ACTIVITY.read_text()
        self.assertLess(
            source.index('command in setOf("descargas"'),
            source.index('contentPort != null -> sendWebCommand(command)'),
        )
        self.assertIn('"abrir descargas"', source)
        self.assertIn('"cerrar navegador"', source)
        self.assertIn('"guardar en favoritos"', source)

    def test_voice_configuration_and_domain_input(self):
        source = ACTIVITY.read_text()
        for marker in (
            'private fun spanishResponseVoices()',
            'private fun configureResponseVoice()',
            'private fun chooseResponseVoice()',
            'prefs.getString("tts_voice_name", null)',
            'Normalizer.normalize(', 'SpeechRecognizer', 'Conservar puntos',
        ):
            self.assertIn(marker, source)

    def test_android_manifest_exposes_recognition_and_tts_services(self):
        manifest = (ROOT / "android/app/src/main/AndroidManifest.xml").read_text()
        self.assertIn("android.speech.RecognitionService", manifest)
        self.assertIn("android.intent.action.TTS_SERVICE", manifest)

    def test_documentation_describes_actual_scope(self):
        guide = DOC.read_text()
        readme = README.read_text()
        for example in (
            "buscar universidades", "abrir descargas", "ayuda",
            "voz femenina", "cursor siguiente", "salir", "privacidad",
        ):
            self.assertIn(example.lower(), guide.lower())
        self.assertIn("docs/GUIA_COMANDOS_ACCESSIA.md", readme)
        self.assertIn("GENAI INDUSTRIES", readme)


if __name__ == "__main__":
    unittest.main()
