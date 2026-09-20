#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path

TARGET = Path("app/src/main/java/com/saney/ytmimporter/MenuActivity.kt")

OLD = """                setOnClickListener {
                    when (action) {
                        ACTION_THEME ->
                            showThemePicker()

                        ACTION_REPLACEMENTS ->
                            showReplacementLog()

                        ACTION_OPEN_YTM ->
                            openTargetInYtm()

                        ACTION_DATA ->
                            startActivity(
                                Intent(
                                    this@MenuActivity,
                                    DataActivity::class.java
                                )
                            )

                        ACTION_SERVICE ->
                            openServiceTools()

                        else -> {
                            setResult(
                                RESULT_OK,
                                Intent().putExtra(
                                    EXTRA_ACTION,
                                    action
                                )
                            )
                            finish()
                            overridePendingTransition(0, 0)
                        }
                    }
                }
"""

NEW = """                setOnClickListener {
                    if (action == ACTION_THEME) {
                        showThemePicker()
                    } else {
                        when (action) {
                            ACTION_REPLACEMENTS ->
                                showReplacementLog()

                            ACTION_OPEN_YTM ->
                                openTargetInYtm()

                            ACTION_DATA ->
                                startActivity(
                                    Intent(
                                        this@MenuActivity,
                                        DataActivity::class.java
                                    )
                                )

                            ACTION_SERVICE ->
                                openServiceTools()

                            else -> {
                                setResult(
                                    RESULT_OK,
                                    Intent().putExtra(
                                        EXTRA_ACTION,
                                        action
                                    )
                                )
                                finish()
                                overridePendingTransition(0, 0)
                            }
                        }
                    }
                }
"""

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    if not TARGET.exists():
        raise SystemExit(f"FAIL: missing {TARGET}")

    text = TARGET.read_text(encoding="utf-8")
    old_count = text.count(OLD)
    new_count = text.count(NEW)

    if new_count == 1:
        print("SKIP: historical Menu Theme contract already restored")
        return

    if old_count != 1 or new_count != 0:
        raise SystemExit(
            f"FAIL: FIX5 anchor mismatch: old={old_count}, new={new_count}"
        )

    print("READY: restore historical if (action == ACTION_THEME) contract")
    if not args.check:
        TARGET.write_text(
            text.replace(OLD, NEW, 1),
            encoding="utf-8",
            newline="\n",
        )
        print("APPLIED: Menu Theme historical source contract restored")

if __name__ == "__main__":
    main()
