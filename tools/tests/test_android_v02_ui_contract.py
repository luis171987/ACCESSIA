from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]


class AccessiaV02Contract(unittest.TestCase):
    def test_branding(self):
        manifest = (ROOT / "android/app/src/main/AndroidManifest.xml").read_text()
        self.assertIn('@drawable/accessia_icon', manifest)
        self.assertTrue((ROOT / "android/app/src/main/res/drawable-nodpi/accessia_icon.webp").is_file())
        self.assertTrue((ROOT / "docs/assets/accessia-logo-oficial.png").is_file())

    def test_voice_and_floating_ui(self):
        source = (ROOT / "android/app/src/main/java/org/accessia/browser/MainActivity.kt").read_text()
        for string in (
            '"salir"', '"mostrar controles flotantes"',
            '"ocultar controles flotantes"', 'RecognizerIntent.EXTRA_MAX_RESULTS',
            'fun chooseVoiceLanguage()', 'fun showDownloads()',
            'fun showLinks(', 'fun toggleFavorite()', '"about:blank"',
            'GeckoSession.NavigationDelegate',
        ):
            self.assertIn(string, source)
        self.assertIn('speech', source.lower())

    def test_readme(self):
        data = (ROOT / "README.md").read_text()
        self.assertIn('docs/assets/accessia-logo-oficial.png', data)
        self.assertIn('GENAI INDUSTRIES', data)


if __name__ == "__main__":
    unittest.main()
