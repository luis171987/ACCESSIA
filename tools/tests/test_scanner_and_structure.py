"""Tests del contrato documental y del detector público de secretos."""
from pathlib import Path
import importlib.util
import unittest

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("secret_scan", ROOT / "tools/check_public_secrets.py")
scanner = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(scanner)


class RepositoryContractTests(unittest.TestCase):
    def test_all_twelve_mermaid_diagrams(self):
        diagrams = sorted((ROOT / "docs/02-diagramas").glob("[0-9][0-9]-*.md"))
        self.assertEqual(len(diagrams), 12)
        for n, path in enumerate(diagrams, 1):
            self.assertTrue(path.name.startswith(f"{n:02d}-"), path.name)
            content = path.read_text(encoding="utf-8")
            self.assertIn("```mermaid", content)
            self.assertIn("GENAI INDUSTRIES", content)

    def test_main_index_links_exist(self):
        index = ROOT / "docs/00-INDICE-MAESTRO.md"
        import re
        for target in re.findall(r"\]\(([^)]+\.md)\)", index.read_text(encoding="utf-8")):
            self.assertTrue((index.parent / target).is_file(), target)

    def test_author_and_windows_user_profile(self):
        for item in ["README.md", "LICENSE", "SECURITY.md", "windows/Accessia.Windows/Accessia.Windows.csproj"]:
            self.assertIn("GENAI INDUSTRIES", (ROOT / item).read_text(encoding="utf-8"), item)
        win = (ROOT / "windows/Accessia.Windows/Program.cs").read_text(encoding="utf-8")
        self.assertIn("LocalApplicationData", win)
        self.assertIn("userDataFolder: profilePath", win)


class SecretScannerTests(unittest.TestCase):
    def test_private_key_detected(self):
        x = "-----BEGIN " + "PRIVATE KEY-----"
        self.assertTrue(scanner.scan_text(x))

    def test_long_github_token_detected_without_exposing_it(self):
        tok = "ghp" + "_" + "A" * 36
        result = scanner.scan_text("x = '" + tok + "'")
        self.assertEqual([kind for _, kind in result], ["github-token"])

    def test_plain_language_not_flagged(self):
        self.assertEqual(scanner.scan_text("No publiques contraseñas ni tokens; perfil protegido por el SO."), [])


if __name__ == "__main__":
    unittest.main()
