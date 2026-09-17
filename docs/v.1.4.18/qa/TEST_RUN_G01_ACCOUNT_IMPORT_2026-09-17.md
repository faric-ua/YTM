# v1.4.18 — Phone Test Run: Account Playlist → Local Workspace → YTM Project

Date: **2026-09-17**  
Release: **v1.4.18**  
Version code: **52**  
Result: **PASS for the tested G01 path**

## Scope

This manual phone run verifies:

**connected YouTube/YTM account → choose one playlist → load exact tracks → Review without automatic search → save/reopen as YTM Project.**

Test playlist: **`top 3`**, shown as **3 tracks • private**.

## Build

- feature commit: `28ad797b52df73595186e08e36d19a7a0afc4a22`
- GitHub Actions workflow: `Build Signed Android APK`
- run number: **59**
- run id: **35167887857**
- trigger: `workflow_dispatch`
- conclusion: **success**

The resulting v1.4.18 APK was installed before the phone test.

## Steps and results

| Step | Action | Expected | Observed | Result |
|---|---|---|---|---|
| 1 | Open Import with Google/YTM connected | Account import section available | New YouTube/YTM import path available | PASS |
| 2 | Open account playlist picker | Account playlists load | Title/count/privacy shown for normal labels | PASS |
| 3 | Select `top 3` | 3 items load locally | `top 3 • 3 треків` | PASS |
| 4 | Inspect import summary | Exact IDs retained | `точних videoId: 3`, `playlistItems.list: 1 request(s)` | PASS |
| 5 | Tap `3. Знайти / перевірити` | Review opens directly | 3 ready tracks shown | PASS |
| 6 | Verify automatic search | No automatic `search.list` | No Search plan during normal Step 3 path | PASS |
| 7 | Manually tap `↻ Пошук` | Explicit repeat search may request quota | 3 new `search.list` shown only after manual action | PASS / expected |
| 8 | Save as YTM Project | Exact selections persist | Saved project could be reopened | PASS |
| 9 | Reopen YTM Project | Exact IDs remain | `3 треків`, `Точних videoId: 3`, `Без videoId: 0` | PASS |
| 10 | Inspect reopened track | Ready result remains | Found video still preserved | PASS |

## Important clarification

The screenshot showing `Пошук потрібен для: 3` and `Потрібно нових search.list: 3` was produced **only after manually pressing `↻ Пошук`** inside Review.

It is not an automatic-search regression.

## Not verified by this run

- no-auth-token error path;
- exact source-order comparison;
- remote source immutability through before/after refresh;
- file/TXT import regression;
- Destination regression in v1.4.18;
- rotation regression in v1.4.18;
- BUG-003 / BUG-004 closure.

## Final result

**PASS — v1.4.18 G01 works on the tested phone path.**
