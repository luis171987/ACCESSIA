#!/usr/bin/env python3
"""Heuristic public-source secret scanner. Prints locations, never matched values.

This is NOT a substitute for GitHub Secret Scanning, Gitleaks or a human review,
and does not examine Git history, previously published releases or images.
"""
from __future__ import annotations

import argparse
from pathlib import Path
import re
import subprocess
import sys

RULES = {
    "private-key-header": re.compile(r"-----BEGIN (?:RSA |OPENSSH |EC |DSA |ENCRYPTED )?PRIVATE KEY-----"),
    "github-token": re.compile(r"\b(?:ghp|gho|ghu|ghs|ghr)_[A-Za-z0-9]{30,}\b|\bgithub_pat_[A-Za-z0-9_]{40,}\b"),
    "aws-access-key": re.compile(r"\b(?:AKIA|ASIA)[0-9A-Z]{16}\b"),
    "bearer-token": re.compile(r"(?i)\bAuthorization\s*[:=]\s*['\"]?Bearer\s+[A-Za-z0-9._~+/-]{20,}"),
    "assigned-credential": re.compile(r"(?i)\b(?:password|passwd|api_key|access_token|secret_key|private_token)\s*[:=]\s*['\"]?([A-Za-z0-9/+_=.-]{16,})"),
}
SKIP_SUFFIXES = {".png", ".jpg", ".jpeg", ".webp", ".gif", ".mp4", ".zip", ".pdf", ".ico"}
MAX_SIZE = 2_000_000


def scan_text(contents: str) -> list[tuple[int, str]]:
    findings = []
    for line_no, line in enumerate(contents.splitlines(), 1):
        for rule_name, pattern in RULES.items():
            if pattern.search(line):
                findings.append((line_no, rule_name))
    return findings


def tracked_files(root: Path) -> list[Path]:
    result = subprocess.run(
        ["git", "ls-files", "--cached", "-z"], cwd=root, check=True,
        capture_output=True
    )
    return [root / p.decode("utf-8", "surrogateescape") for p in result.stdout.split(b"\0") if p]


def main() -> int:
    parser = argparse.ArgumentParser(description="Analyze tracked ACCESSIA text files")
    parser.add_argument("--repo-root", type=Path, default=Path(__file__).resolve().parents[1])
    args = parser.parse_args()
    root = args.repo_root.resolve()
    total = 0
    failures = 0
    for file in tracked_files(root):
        if not file.exists() or file.suffix.lower() in SKIP_SUFFIXES or file.stat().st_size > MAX_SIZE:
            continue
        try:
            data = file.read_text(encoding="utf-8")
        except (UnicodeDecodeError, OSError):
            continue
        total += 1
        for line, kind in scan_text(data):
            failures += 1
            print(f"BLOQUEADO: {file.relative_to(root)}:{line} [{kind}] (valor oculto)")
    print(f"Archivos de texto examinados: {total}. Hallazgos: {failures}.")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
