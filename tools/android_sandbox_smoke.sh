#!/usr/bin/env bash
set -euo pipefail

# This script can install/copy only into an explicitly addressed ephemeral AVD.
[[ "${GITHUB_ACTIONS:-}" == "true" ]] || { echo 'Requires a GitHub Actions sandbox.'; exit 1; }
accessia_api="${1:?Android API required}"
case "$accessia_api" in 28|35|36) ;; *) echo 'Unsupported sandbox API'; exit 1;; esac
accessia_adb="$ANDROID_HOME/platform-tools/adb"
accessia_serial='emulator-5554'
[[ "$("$accessia_adb" -s "$accessia_serial" shell getprop ro.kernel.qemu | tr -d '\r')" == '1' ]] || { echo 'Refusing a non-emulator device.'; exit 1; }
accessia_extra=()
if [[ "$accessia_api" != '28' ]]; then
  gradle -p android :app:assembleDebug :app:assembleDebugAndroidTest --no-daemon
  accessia_model="$RUNNER_TEMP/accessia-sandbox-qwen.litertlm"
  curl --fail --silent --show-error --location 'https://huggingface.co/litert-community/Qwen3-0.6B/resolve/e6687177e00897731e7716267eebd4eba0dde7c2/Qwen3-0.6B.litertlm' --output "$accessia_model"
  python -c 'import hashlib,sys;from pathlib import Path;p=Path(sys.argv[1]);assert p.stat().st_size==613722264;f=p.open("rb");assert hashlib.file_digest(f,"sha256").hexdigest()=="c5f39b4191926786c3e6668b62af4d6eb15248dfc7a5348252a9b406f33f2108"' "$accessia_model"
  "$accessia_adb" -s "$accessia_serial" install -r android/app/build/outputs/apk/debug/app-debug.apk
  "$accessia_adb" -s "$accessia_serial" shell run-as org.accessia.browser.beta03 mkdir -p files/local-agent
  "$accessia_adb" -s "$accessia_serial" shell -T "run-as org.accessia.browser.beta03 sh -c 'cat > files/local-agent/qwen3-0.6b.litertlm'" < "$accessia_model"
  accessia_extra+=('-Pandroid.testInstrumentationRunnerArguments.realModelSmoke=true')
fi
gradle -p android :app:connectedDebugAndroidTest -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true "${accessia_extra[@]}" --no-daemon
mkdir -p sandbox-evidence
for accessia_view in 01-inicio 02-menu 03-asistente; do
  "$accessia_adb" -s "$accessia_serial" exec-out run-as org.accessia.browser.beta03 cat "files/sandbox-$accessia_view.png" > "sandbox-evidence/android-$accessia_api-$accessia_view.png"
done
python -c 'from pathlib import Path;files=list(Path("sandbox-evidence").glob("*.png"));assert len(files)==3;assert all(p.stat().st_size>100 and p.read_bytes().startswith(bytes([137,80,78,71,13,10,26,10])) for p in files), "Invalid emulator PNG capture"'
