#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TARGET = ROOT / "scripts/v1447-r3-lifecycle-wave1-audit.sh"

OLD_GUARD = r'''GRADLE="app/build.gradle.kts"

for f in "$UI" "$SELECTOR" "$RECENT" "$STORAGE" "$REVIEW" "$MENU" "$IMPORT" "$DATA" "$PLAYLIST" "$GRADLE"; do
  test -f "$f" || fail "missing lifecycle audit file: $f"
done

# Wave 1 intentionally stays on the R2 application identity until the whole R3 fix wave is ready.
grep -Fq 'versionCode = 89' "$GRADLE" || fail "Wave 1 unexpectedly changed versionCode"
grep -Fq 'versionName = "1.4.47-R2"' "$GRADLE" || fail "Wave 1 unexpectedly changed versionName"
'''

NEW_GUARD = r'''WAVE_DOC="docs/v.1.4.47/qa/R3_LIFECYCLE_WAVE1.md"

for f in "$UI" "$SELECTOR" "$RECENT" "$STORAGE" "$REVIEW" "$MENU" "$IMPORT" "$DATA" "$PLAYLIST" "$WAVE_DOC"; do
  test -f "$f" || fail "missing lifecycle audit file: $f"
done

# Historical Wave 1 intentionally did not bump the app identity. Keep that fact
# in immutable wave documentation instead of pinning the current app forever to R2.
grep -Fq 'does **not** change `versionName` / `versionCode` yet' "$WAVE_DOC" ||
  fail "Wave 1 historical no-version-bump boundary missing"
'''

OLD_ECHO = r'''echo "- Wave 1 keeps app identity at v1.4.47-R2 / code 89 until full R3 scope is complete"'''

NEW_ECHO = r'''echo "- Wave 1 historical no-version-bump boundary is documented without pinning the current app identity"'''


def replace_exact(text: str, old: str, new: str, name: str, apply: bool) -> str:
    old_count = text.count(old)
    new_count = text.count(new)

    if new_count == 1:
        print(f"SKIP: already applied: {name}")
        return text

    if old_count == 1 and new_count == 0:
        print(f"READY: {name}")
        if apply:
            print(f"APPLIED: {name}")
            return text.replace(old, new, 1)
        return text

    raise SystemExit(
        f"FAIL: anchor mismatch for {name}: old={old_count}, new={new_count}, "
        f"file={TARGET.relative_to(ROOT)}"
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    text = TARGET.read_text(encoding="utf-8")
    updated = replace_exact(
        text,
        OLD_GUARD,
        NEW_GUARD,
        "Wave 1 historical version guard",
        apply=not args.check,
    )
    updated = replace_exact(
        updated,
        OLD_ECHO,
        NEW_ECHO,
        "Wave 1 audit result wording",
        apply=not args.check,
    )

    if not args.check and updated != text:
        TARGET.write_text(updated, encoding="utf-8", newline="\n")

    if args.check:
        print("PASS: consolidation FIX1 anchors are ready or already applied")
    else:
        print("PASS: consolidation FIX1 applied")


if __name__ == "__main__":
    main()
