from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[2]
KOTLIN = (ROOT / "android/app/src/main/java/org/accessia/browser/MainActivity.kt")
README = ROOT / "README.md"
DOC = ROOT / "docs/ACCESSIA_V03_VOZ_IDIOMAS_TRADUCCION.md"


class SpeechAndMultilingualContract(unittest.TestCase):
    def test_long_dictation_keeps_text_and_waits_for_user(self):
        source = KOTLIN.read_text()
        self.assertIn('actionButton("Dictar +") { toggleLongDictation() }', source)
        self.assertIn('private fun appendDictatedSegment(segment: String)', source)
        self.assertIn('private var longDictationEnabled = false', source)
        self.assertIn('address.setText(if (old.isEmpty()) fragment else old + " " + fragment)', source)
        self.assertIn('if (longDictationEnabled) appendDictatedSegment(spoken)', source)
        self.assertIn('if (longDictationEnabled) finishLongDictation()', source)

    def test_speech_partial_not_executed_and_advanced_options_optional(self):
        source = KOTLIN.read_text()
        self.assertIn('override fun onPartialResults(partialResults: Bundle?)', source)
        self.assertIn('lastPartialTranscript = phrase', source)
        self.assertIn('EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS', source)
        self.assertIn('EXTRA_ENABLE_LANGUAGE_DETECTION', source)
        self.assertIn('val alternatives = results?', source)

    def test_languages_device_default_and_manual_override(self):
        source = KOTLIN.read_text()
        self.assertIn('resources.configuration.locales.get(0)', source)
        self.assertIn('private fun pickLanguage(', source)
        self.assertIn('Código de idioma BCP 47', source)
        self.assertIn('runtime.settings.setLocales(arrayOf(tag))', source)
        self.assertIn('private fun chooseSiteLanguage()', source)

    def test_ip_consent_and_secure_remote_translation(self):
        source = KOTLIN.read_text()
        self.assertIn('if (!prefs.contains("ip_region_consent")) askIpRegionPermission()', source)
        self.assertIn('putBoolean("ip_region_consent", false)', source)
        self.assertIn('URL("https://ipapi.co/country/")', source)
        self.assertIn('as HttpsURLConnection', source)
        self.assertIn('private fun translateCurrentPage()', source)
        self.assertIn('Google Traductor', source)
        self.assertIn('URL', source)
        self.assertNotIn('ACCESS_FINE_LOCATION', source)

    def test_beta_isolated_from_previous_android_install(self):
        gradle = (ROOT / "android/app/build.gradle.kts").read_text()
        self.assertIn('applicationIdSuffix = ".beta03"', gradle)
        self.assertIn('versionName = "0.3.0-dev"', gradle)
        self.assertIn('manifestPlaceholders["appLabel"] = "ACCESSIA Beta 0.3"', gradle)

    def test_documentation_explicitly_limits_features(self):
        source = DOC.read_text()
        self.assertIn('reconocimiento streaming', source)
        self.assertIn('deuda pendiente', source)
        self.assertIn('SofiaNeural', source)
        self.assertIn('DaliaNeural', source)
        self.assertIn('ipapi.co', source)
        self.assertIn('Piper', source)
        self.assertIn('sherpa-onnx', source)
        self.assertIn('docs/ACCESSIA_V03_VOZ_IDIOMAS_TRADUCCION.md', README.read_text())


if __name__ == "__main__":
    unittest.main()
