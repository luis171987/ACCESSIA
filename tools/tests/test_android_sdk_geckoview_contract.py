from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]

class GeckoViewBuildContract(unittest.TestCase):

    def test_sdk_minor_2(self):
        source = (
            ROOT / "android/app/build.gradle.kts"
        ).read_text()
        self.assertIn("minorApiLevel = 2", source)

    def test_ci_sdk_37_2(self):
        source = (
            ROOT / ".github/workflows/build-android.yml"
        ).read_text()
        self.assertIn(
            "'platforms;android-37.2'", source
        )

    def test_extension_nullable(self):
        source = (
            ROOT /
            "android/app/src/main/java/org/accessia/browser/MainActivity.kt"
        ).read_text()
        self.assertIn("if (extension == null) {", source)

    def test_error_nullable(self):
        source = (
            ROOT /
            "android/app/src/main/java/org/accessia/browser/MainActivity.kt"
        ).read_text()
        self.assertIn("error?.message", source)
        self.assertNotIn("${error.message}", source)
