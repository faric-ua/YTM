# v1.4.19 — Phone Test Run: Export All Account Playlists

Date: **2026-09-17**  
Release: **v1.4.19**  
Feature commit: `7e89cc7257a401c9a2be79c91d27d1812e9f5e19`  
Result: **PASS for the tested bulk-export path**

## Tested flow

**connected Google/YTM account → choose device folder → export all account playlists → inspect manifest/files → reopen one exported YTM Project → Review exact tracks**

## Observed results

| Check | Observed | Result |
|---|---|---|
| Bulk-export action appears in Import | Button visible | PASS |
| Folder-selection flow returns to app | Export started successfully | PASS |
| Account playlists discovered | 21 | PASS |
| Exported YTM Projects | 21 | PASS |
| Skipped playlists | 0 | PASS |
| Failed playlists | 0 | PASS |
| `playlistItems.list` requests | 33 | PASS |
| Export session folder | Created | PASS |
| Session-folder objects | 22 = 21 projects + `manifest.json` | PASS |
| Manifest summary | 21 / 21 / 0 / 0 / 33 | PASS |
| Reopen one exported project | `mylist`, 2 tracks | PASS |
| Exact IDs after reopen | 2/2 | PASS |
| Missing IDs after reopen | 0 | PASS |
| Step 3 after reopen | Review opens with both tracks ready | PASS |

## Source safety

The bulk-export implementation is statically guarded as read-only and does not call the remote playlist create/add write operations.

A separate phone-side before/after refresh of source playlist title/count/privacy was **not** captured in this run, so this report does not claim that specific regression item as phone-verified.

## UI observation

The account-import card has a non-blocking layout problem:

- **`Експортувати всі плейлисти в папку`** is vertically clipped;
- the two account action buttons have almost no vertical gap.

This is scheduled for the next patch release.

## Conclusion

**v1.4.19 = PARTIALLY PHONE-TESTED — PASS FOR BULK EXPORT PATH.**
