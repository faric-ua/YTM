#!/usr/bin/env python3
from __future__ import annotations
import argparse
from pathlib import Path

OPS = [{'file': 'app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt', 'name': 'Destination Toast import', 'old': 'import android.widget.ScrollView\nimport android.widget.TextView\n', 'new': 'import android.widget.ScrollView\nimport android.widget.TextView\nimport android.widget.Toast\n'}, {'file': 'app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt', 'name': 'Destination stable progress dialog show', 'old': '        remoteProgressDialog =\n            UiChrome\n                .alertBuilder(this)\n                .setTitle(\n                    if (\n                        state.kind ==\n                        DestinationRemoteOperations\n                            .Kind.LOAD_PLAYLISTS\n                    ) {\n                        "Існуючий плейлист"\n                    } else {\n                        "Перевірка перед додаванням"\n                    }\n                )\n                .setView(\n                    content\n                )\n                .create()\n                .also {\n                    dialog ->\n                    dialog.setCancelable(\n                        false\n                    )\n                    dialog.show()\n                }\n', 'new': '        remoteProgressDialog =\n            UiChrome\n                .alertBuilder(this)\n                .setTitle(\n                    if (\n                        state.kind ==\n                        DestinationRemoteOperations\n                            .Kind.LOAD_PLAYLISTS\n                    ) {\n                        "Існуючий плейлист"\n                    } else {\n                        "Перевірка перед додаванням"\n                    }\n                )\n                .setView(\n                    content\n                )\n                .show()\n                .also {\n                    dialog ->\n                    dialog.setCancelable(\n                        false\n                    )\n                }\n'}, {'file': 'app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt', 'name': 'Destination toast helper', 'old': '    private fun dp(value: Int): Int =\n        (\n            value *\n                resources.displayMetrics.density\n        ).toInt()\n', 'new': '    private fun toast(\n        message: String\n    ) {\n        Toast\n            .makeText(\n                this,\n                message,\n                Toast.LENGTH_LONG\n            )\n            .show()\n    }\n\n    private fun dp(value: Int): Int =\n        (\n            value *\n                resources.displayMetrics.density\n        ).toInt()\n'}, {'file': 'app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt', 'name': 'Review stable progress dialog show', 'old': '        remoteProgressDialog =\n            UiChrome\n                .alertBuilder(this)\n                .setTitle(\n                    if (\n                        state.kind ==\n                        ReviewRemoteOperations\n                            .Kind.MANUAL_URL\n                    ) {\n                        "Ручне посилання"\n                    } else {\n                        "Пошук треків"\n                    }\n                )\n                .setView(\n                    content\n                )\n                .create()\n                .also {\n                    dialog ->\n                    dialog.setCancelable(\n                        false\n                    )\n                    dialog.show()\n                }\n', 'new': '        remoteProgressDialog =\n            UiChrome\n                .alertBuilder(this)\n                .setTitle(\n                    if (\n                        state.kind ==\n                        ReviewRemoteOperations\n                            .Kind.MANUAL_URL\n                    ) {\n                        "Ручне посилання"\n                    } else {\n                        "Пошук треків"\n                    }\n                )\n                .setView(\n                    content\n                )\n                .show()\n                .also {\n                    dialog ->\n                    dialog.setCancelable(\n                        false\n                    )\n                }\n'}, {'file': 'app/src/main/java/com/saney/ytmimporter/destination/DestinationForwardedWritePlan.kt', 'name': 'Primitive skip positions compatibility', 'old': '        val skippedPositions =\n            data.getIntArrayExtra(\n                DestinationActivity\n                    .EXTRA_LOCAL_SKIP_POSITIONS\n            )\n                .orEmpty()\n                .toSet()\n', 'new': '        val skippedPositions =\n            (\n                data.getIntArrayExtra(\n                    DestinationActivity\n                        .EXTRA_LOCAL_SKIP_POSITIONS\n                ) ?: IntArray(0)\n            )\n                .toSet()\n'}, {'file': 'scripts/release-preflight.sh', 'name': 'Release preflight runs R4 FIX2 audit', 'old': 'bash scripts/v1447-r3-navigation-ownership-audit.sh\nbash scripts/project-handoff-audit.sh\n', 'new': 'bash scripts/v1447-r3-navigation-ownership-audit.sh\nbash scripts/v1447-r3-navigation-ownership-r4-fix2-audit.sh\nbash scripts/project-handoff-audit.sh\n'}, {'file': 'scripts/release-preflight.sh', 'name': 'Release preflight checks R4 FIX2 audit', 'old': 'check_file "scripts/v1447-r3-navigation-ownership-audit.sh"\ncheck_file "app/src/main/java/com/saney/ytmimporter/review/ReviewRemoteOperations.kt"\n', 'new': 'check_file "scripts/v1447-r3-navigation-ownership-audit.sh"\ncheck_file "scripts/v1447-r3-navigation-ownership-r4-fix2-audit.sh"\ncheck_file "app/src/main/java/com/saney/ytmimporter/review/ReviewRemoteOperations.kt"\n'}]
NEW_FILES = {'scripts/v1447-r3-navigation-ownership-r4-fix2-audit.sh': '#!/usr/bin/env bash\nset -euo pipefail\n\nfail(){ echo "FAIL: $1" >&2; exit 1; }\n\nDEST="app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"\nREVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"\nPLAN="app/src/main/java/com/saney/ytmimporter/destination/DestinationForwardedWritePlan.kt"\n\nfor f in "$DEST" "$REVIEW" "$PLAN"; do\n  test -f "$f" || fail "missing R4 FIX2 source: $f"\ndone\n\ngrep -Fq \'import android.widget.Toast\' "$DEST" ||\n  fail "Destination Toast import missing"\ngrep -Fq \'private fun toast(\' "$DEST" ||\n  fail "Destination toast helper missing"\n\npython - "$DEST" "$REVIEW" "$PLAN" <<\'PY_AUDIT\'\nfrom pathlib import Path\nimport sys\n\ndest, review, plan = [\n    Path(p).read_text(encoding="utf-8")\n    for p in sys.argv[1:]\n]\n\nfor name, text in [("Destination", dest), ("Review", review)]:\n    start = text.index("private fun showRemoteProgress(")\n    end = text.index("\\n    private fun ", start + 10)\n    block = text[start:end]\n\n    if ".create()" in block:\n        raise SystemExit(\n            f"FAIL: {name} still calls unsupported StableAlertBuilder.create()"\n        )\n    if ".show()" not in block:\n        raise SystemExit(\n            f"FAIL: {name} remote progress dialog does not use StableAlertBuilder.show()"\n        )\n    if "setCancelable(" not in block:\n        raise SystemExit(\n            f"FAIL: {name} remote progress dialog lost non-cancelable contract"\n        )\n\nif ".orEmpty()" in plan:\n    raise SystemExit(\n        "FAIL: forwarded primitive IntArray still uses unsupported nullable orEmpty()"\n    )\nif "?: IntArray(0)" not in plan:\n    raise SystemExit(\n        "FAIL: forwarded skip positions do not use explicit IntArray fallback"\n    )\nif "index in" not in plan or "index !in" not in plan:\n    raise SystemExit(\n        "FAIL: forwarded duplicate include/exclude contracts missing"\n    )\nPY_AUDIT\n\necho "PASS:"\necho "- Destination toast helper present"\necho "- StableAlertBuilder uses supported show() API in Destination/Review"\necho "- remote progress dialogs remain non-cancelable"\necho "- primitive skip positions use explicit IntArray fallback"\n', 'docs/v.1.4.47/qa/R3_NAVIGATION_OWNERSHIP_R4_FIX2.md': '# v1.4.47-R3 Navigation Ownership R4 — FIX2A\n\nThis package supersedes the first FIX2 package, whose apply script used the\nwrong import anchor (`android.widget.TextWatcher`). DestinationActivity imports\n`android.text.TextWatcher`, so that package failed closed before changing code.\n\nThe compile corrections themselves are unchanged:\n- add Destination Toast import/helper;\n- use `UiChrome.StableAlertBuilder.show()` instead of unsupported `.create()`;\n- keep returned progress dialogs non-cancelable;\n- replace primitive `IntArray?` `.orEmpty()` with `?: IntArray(0)`;\n- run a dedicated compile-contract audit from release preflight.\n\nSource base remains commit `a049d953a4eb5f9df939eac8d9bb766c095ed697`.\n'}

def patch_one(op, do_apply):
    path = Path(op["file"])
    if not path.exists():
        raise SystemExit(f"FAIL: missing file: {path}")

    text = path.read_text(encoding="utf-8")
    old, new = op["old"], op["new"]
    old_count, new_count = text.count(old), text.count(new)

    # Some "new" forms intentionally contain the old text as a prefix.
    if new_count == 1 and old_count in (0, 1):
        print(f"SKIP: already applied: {op['name']}")
        return

    if old_count != 1 or new_count != 0:
        raise SystemExit(
            f"FAIL: FIX2A anchor mismatch for {op['name']}: "
            f"old={old_count}, new={new_count}"
        )

    print(f"READY: {op['name']}")
    if do_apply:
        path.write_text(
            text.replace(old, new, 1),
            encoding="utf-8",
            newline="\n",
        )
        print(f"APPLIED: {op['name']}")

def ensure_file(rel, content, do_apply):
    path = Path(rel)

    if path.exists():
        current = path.read_text(encoding="utf-8")
        if current == content:
            print(f"SKIP: already present: {rel}")
            return

        # FIX2A is allowed to overwrite only the uncommitted generated FIX2
        # evidence/audit files from the superseded package.
        if rel in NEW_FILES:
            print(f"READY: replace superseded generated file: {rel}")
            if do_apply:
                path.write_text(content, encoding="utf-8", newline="\n")
                print(f"REPLACED: {rel}")
            return

        raise SystemExit(f"FAIL: existing generated file differs: {rel}")

    print(f"READY: create {rel}")
    if do_apply:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8", newline="\n")
        print(f"CREATED: {rel}")

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    for op in OPS:
        patch_one(op, not args.check)

    for rel, content in NEW_FILES.items():
        ensure_file(rel, content, not args.check)

    print(
        "PASS: R4 FIX2A ready/already applied"
        if args.check
        else "PASS: R4 FIX2A applied"
    )

if __name__ == "__main__":
    main()
