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
            f"STOP: expected exactly 1 anchor in {rel}, found {count}\nANCHOR:\n{old}"
        )
    write(rel, text.replace(old, new, 1))
    print(f"PATCH: {rel}")

old = "grep -Fq '| v1.4.18 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\\n  || fail \"v1.4.18 must start NOT TESTED YET\""
new = "grep -Fq '| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** |' RELEASE_TEST_STATUS.md \\\n  || fail \"v1.4.18 G01 phone-test PASS status missing\""

replace_once(
    "scripts/release-preflight.sh",
    old,
    new,
)

apply_rel = "scripts/apply-v1.4.18-phone-qa.py"
apply_text = read(apply_rel)

if "release-preflight v1.4.18 status anchor missing" not in apply_text:
    marker = 'print("PASS: v1.4.18 phone QA status updates applied")'
    if marker not in apply_text:
        raise SystemExit("STOP: phone-QA apply script marker missing")

    block = '''
preflight_path = "scripts/release-preflight.sh"
preflight = read(preflight_path)
old_preflight = "grep -Fq '| v1.4.18 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\\\n  || fail \\\"v1.4.18 must start NOT TESTED YET\\\""
new_preflight = "grep -Fq '| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** |' RELEASE_TEST_STATUS.md \\\\n  || fail \\\"v1.4.18 G01 phone-test PASS status missing\\\""
if new_preflight not in preflight:
    if old_preflight not in preflight:
        raise SystemExit("STOP: release-preflight v1.4.18 status anchor missing")
    write(preflight_path, preflight.replace(old_preflight, new_preflight, 1))
    print("PATCH: scripts/release-preflight.sh")
'''
    apply_text = apply_text.replace(marker, block + "\n" + marker, 1)
    write(apply_rel, apply_text)
    print("PATCH: scripts/apply-v1.4.18-phone-qa.py")
else:
    print("SKIP already updated: scripts/apply-v1.4.18-phone-qa.py")

print("PASS: release-status audit guard fixed")
