#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")

def write(rel, text):
    (ROOT / rel).write_text(text, encoding="utf-8")

def replace_once(rel, old, new):
    text = read(rel)
    if new in text:
        print(f"SKIP already fixed: {rel}")
        return
    count = text.count(old)
    if count != 1:
        raise SystemExit(
            f"STOP: expected exactly 1 anchor in {rel}, found {count}"
        )
    write(rel, text.replace(old, new, 1))
    print(f"PATCH: {rel}")

old_picker = r'''        UiChrome.alertBuilder(this)
            .setTitle(
                "Вибрати плейлист YouTube/YTM"
            )
            .setItems(
                labels.toTypedArray()
            ) { _, which ->
                val selected =
                    playlists.getOrNull(which)
                        ?: return@setItems

                loadYtmPlaylist(
                    token = token,
                    playlistInfo = selected
                )
            }
            .setNegativeButton(
                "Скасувати",
                null
            )
            .show()
'''

new_picker = r'''        UiChrome.showMenuDialog(
            activity = this,
            title =
                "Вибрати плейлист YouTube/YTM",
            subtitle =
                "Read-only: виберіть плейлист для локального імпорту.",
            actions =
                playlists.mapIndexed {
                        index,
                        playlist ->

                    UiChrome.MenuAction(
                        label = labels[index],
                        onClick = {
                            loadYtmPlaylist(
                                token = token,
                                playlistInfo =
                                    playlist
                            )
                        }
                    )
                },
            negativeLabel = "Скасувати"
        )
'''

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    old_picker,
    new_picker,
)

apply_rel = "scripts/apply-v1.4.18.py"
apply_text = read(apply_rel)

if new_picker not in apply_text:
    if old_picker not in apply_text:
        raise SystemExit(
            "STOP: scripts/apply-v1.4.18.py picker template anchor missing"
        )
    apply_text = apply_text.replace(
        old_picker,
        new_picker,
        1,
    )
    write(apply_rel, apply_text)
    print("PATCH: scripts/apply-v1.4.18.py")
else:
    print("SKIP already fixed: scripts/apply-v1.4.18.py")

audit_rel = "scripts/v1418-account-library-import-audit.sh"
audit = read(audit_rel)

anchor = '''grep -Fq 'Вибрати плейлист з YTM' "$IMPORT" || fail "playlist picker CTA missing"
'''
addition = '''grep -Fq 'Вибрати плейлист з YTM' "$IMPORT" || fail "playlist picker CTA missing"
grep -q 'UiChrome.showMenuDialog' "$IMPORT" || fail "styled playlist picker missing"
if grep -q '\\.setItems(' "$IMPORT"; then
  fail "raw AlertDialog.setItems remains in ImportActivity"
fi
'''

if "styled playlist picker missing" not in audit:
    if anchor not in audit:
        raise SystemExit(
            "STOP: v1418 audit picker anchor missing"
        )
    audit = audit.replace(
        anchor,
        addition,
        1,
    )
    write(audit_rel, audit)
    print("PATCH: scripts/v1418-account-library-import-audit.sh")
else:
    print("SKIP already fixed: scripts/v1418-account-library-import-audit.sh")

print()
print("PASS: v1.4.18 G01A menu fix applied")
