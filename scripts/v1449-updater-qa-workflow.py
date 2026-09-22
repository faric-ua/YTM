#!/usr/bin/env python3
import sys
from pathlib import Path

WORKFLOW = Path(".github/workflows/build-apk.yml")

QA_VERSION_NAME = "1.4.49-updater-qa1"
QA_VERSION_CODE = 93
QA_MANIFEST_URL = (
    "https://github.com/faric-ua/YTM/releases/download/"
    "v1.4.49-updater-qa1/YTM-Importer-update.json"
)

BASE_TRIGGER = "on: [workflow_dispatch]"

PREFLIGHT = """      - name: Release preflight
        shell: bash
        run: bash scripts/release-preflight.sh
"""

FIXTURE_STEP = """
      - name: Apply QA fixture identity
        shell: bash
        run: |
          set -euo pipefail
          grep -Fq 'versionCode = 92' app/build.gradle.kts
          grep -Fq 'versionName = "1.4.49"' app/build.gradle.kts
          sed -i 's/versionCode = 92/versionCode = 93/' app/build.gradle.kts
          sed -i 's/versionName = "1.4.49"/versionName = "1.4.49-updater-qa1"/' app/build.gradle.kts
          grep -Fq 'versionCode = 93' app/build.gradle.kts
          grep -Fq 'versionName = "1.4.49-updater-qa1"' app/build.gradle.kts
"""

CLIENT_STEP = """
      - name: Apply QA updater channel
        shell: bash
        run: |
          set -euo pipefail
          python - <<'PY_QA'
          from pathlib import Path

          remote = Path(
              "app/src/main/java/com/saney/ytmimporter/"
              "updater/UpdaterRemoteOperations.kt"
          )
          text = remote.read_text()

          old = (
              '    const val MANIFEST_URL =\\n'
              '        "https://github.com/faric-ua/YTM/releases/latest/download/" +\\n'
              '            "YTM-Importer-update.json"'
          )
          new = (
              '    const val MANIFEST_URL =\\n'
              '        "https://github.com/faric-ua/YTM/releases/download/'
              'v1.4.49-updater-qa1/YTM-Importer-update.json"'
          )

          if text.count(old) != 1:
              raise SystemExit("stable manifest URL anchor changed")

          remote.write_text(text.replace(old, new, 1))

          service = Path(
              "app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"
          )
          text = service.read_text()

          old = '"Джерело: офіційний GitHub Release faric-ua/YTM."'
          new = '"Джерело: QA GitHub prerelease faric-ua/YTM."'

          if text.count(old) != 1:
              raise SystemExit("stable source-label anchor changed")

          service.write_text(text.replace(old, new, 1))
          PY_QA
"""

def fail(message: str) -> None:
    raise SystemExit(f"STOP: {message}")


def main() -> None:
    if len(sys.argv) != 2 or sys.argv[1] not in {"fixture", "client"}:
        fail("usage: v1449-updater-qa-workflow.py fixture|client")

    mode = sys.argv[1]
    text = WORKFLOW.read_text(encoding="utf-8")

    if BASE_TRIGGER not in text:
        fail("base workflow_dispatch trigger changed")

    if "# QA TEMP BRANCH MODE:" in text:
        fail("workflow already contains a QA temporary-branch marker")

    if text.count(PREFLIGHT) != 1:
        fail("release-preflight workflow anchor changed")

    marker = f"# QA TEMP BRANCH MODE: {mode}\n"

    if text.count("name: Build Signed Android APK\n") != 1:
        fail("workflow name anchor changed")

    text = text.replace(
        "name: Build Signed Android APK\n",
        "name: Build Signed Android APK\n\n" + marker,
        1,
    )

    step = FIXTURE_STEP if mode == "fixture" else CLIENT_STEP

    text = text.replace(
        PREFLIGHT,
        PREFLIGHT + step,
        1,
    )

    WORKFLOW.write_text(
        text,
        encoding="utf-8",
        newline="\n",
    )

    print(f"WROTE QA temporary workflow mode: {mode}")


if __name__ == "__main__":
    main()
