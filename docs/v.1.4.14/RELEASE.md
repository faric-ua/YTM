# YTM Importer v1.4.14 — Session Recovery + Result Modal + Master QA

## Test status

**NOT TESTED YET**

This release must be validated with the new global QA plan before it can be
marked phone-tested.

## 1. Why Google/YTM was asking again after an app update

v1.4.9 intentionally kept the OAuth access token only in process memory.

That protected the token from:
- SharedPreferences;
- YTM Project;
- Full Backup;
- workspace files.

But an in-place app update restarts the Android process. The memory-only token
therefore disappears.

The Google authorization grant itself may still exist, but the app previously
did not automatically ask Google AuthorizationClient for a new access token on
startup. Step 2 therefore looked disconnected until the user pressed it.

## 2. New silent authorization recovery

Added:

`auth/PersistentAuthStateStore.kt`

It stores only one non-secret boolean:

```text
had_successful_authorization = true
```

No token, email or channel identity is stored.

On startup:

```text
memory token exists?
├─ yes → use it
└─ no
   ↓
prior-success marker?
├─ no → normal first-login state
└─ yes
   ↓
AuthorizationClient.authorize()
   ├─ token returned without resolution → restore session automatically
   └─ resolution required → do NOT pop it automatically;
                              ask user to press Step 2
```

This is intended to make ordinary **update-over-existing-app** and normal
process restarts not require repeated account selection when Google still has
an eligible saved account and the required scopes were already granted.

Uninstall/reinstall is different: uninstall clears app data, including the local
prior-success marker.

## 3. Result frame replaced by modal

The large inline result block shown on Home after create/append has been removed.

`showPlaylistResult()` now opens a modal with:

- playlist name;
- added/failed/duplicate counts;
- privacy;
- create/append label;
- YouTube/YTM channel;
- playlist URL;
- `Відкрити в YTM`;
- `Копіювати`;
- `Закрити`.

Closing the modal leaves the Home track list unobstructed.

## 4. Global test system

Added:

- `qa/MASTER_TEST_PLAN.md`
- `qa/TEST_RUN_TEMPLATE.md`
- `qa/TEST_DATA.md`

The master plan covers every major user action:
- install/update;
- import;
- auth;
- search/cache/quota;
- review/manual URL;
- Project save/share;
- new/existing playlist;
- duplicates;
- result;
- Queue/Resume;
- History;
- Backup/Restore/Rollback;
- Service/Diagnostics/Cache;
- UI/rotation;
- privacy/security;
- failure recovery;
- 3-track smoke;
- 50-track stress.

## 5. Test-status register

`RELEASE_TEST_STATUS.md` now records:

- v1.4.12 — NOT TESTED;
- v1.4.13 — PARTIALLY PHONE-TESTED;
- v1.4.14 — NOT TESTED YET.

## 6. Deferred issue

Q-002 custom-dialog entrance motion remains DEFERRED by user.
This release does not reopen that issue.

## Version

```text
versionCode = 48
versionName = "1.4.14"
```
