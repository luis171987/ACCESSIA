# ACCESSIA PR #2 implementation plan

> Execute inline using superpowers:executing-plans; independent review before push.

**Goal:** Correct and answer all 19 review threads on PR #2, verify the branch, push, assess merge readiness, and email the owner an evidence-based report.
**Architecture:** Keep the native Android/GeckoView application and educational constraints. Exercise actual Kotlin behavior with JVM/Robolectric tests, separating UI construction from engine startup only where needed to test native controls.
**Tech stack:** Kotlin, Android API 28+, GeckoView, Gradle, JUnit/Robolectric, Node, Python.
**Spec:** PR #2 inline review threads (19 findings at e947910); user authorization to push, reply, email self, and merge when appropriate.

## Constraints
- Preserve branding, local profile data, consent, and limits on educational automation.
- No production distribution or claimed physical validation without evidence; Honor Magic 7 Lite validation is the existing merge gate.
- Preserve main's current download instructions while resolving branch conflicts.
- No force-push, no automatic third-party voice provider, no added credentials.

## Review focus
- Late recognition callbacks after stopping must not execute commands or alter text.
- Stacked polite prefixes must preserve URL punctuation and search case/accents.
- Small displays, font scaling and hidden/reopened controls must remain usable.
- Stopping listening must bound failures and consistently expose microphone state.
- A closed Gecko session must lose its live history without clearing saved user data.

## Tasks
1. [x] Inspect 19 threads and baseline (12 JS and 24 Python tests); merge current main into the review branch, preserving download documentation.
2. [ ] Add meaningful failing native tests for microphone/dictation transitions, submission, voice parsing, accessible controls, downloaded documents and language changes. Configure CI to run them.
3. [ ] Correct reviewed behavior in MainActivity.kt; verify native tests red then green, full existing suites, APK compilation and secret scan.
4. [ ] Independent code review and any required regression fixes; update D09/D11 and the review/physical-check report.
5. [ ] Commit and push without rewriting history; inspect CI; reply in all 19 threads with evidence and resolve only addressed findings.
6. [ ] Assess merge gate, record remaining physical checks honestly, post PR report and send a final report to the connected Gmail account.

## Progress
- Baseline passed: Node 12/12, Python 24/24.
- Merge conflict limited to README; current main download instructions preserved.
- Inline execution follows user's instruction to complete the work autonomously.
