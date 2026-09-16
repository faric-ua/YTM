# YTM Importer v1.4.6 — Regression checklist

## Upgrade
- [ ] install over v1.4.5 without uninstall;
- [ ] current workspace preserved;
- [ ] History/Data/Queue preserved.

## Two-action confirmations
- [ ] Repeat search keeps simple Cancel / Repeat text actions;
- [ ] Save full backup keeps Cancel / Save;
- [ ] Share backup keeps Cancel / Share;
- [ ] Restore keeps Cancel / Choose backup;
- [ ] buttons do not wrap.

## Three-action dialogs
- [ ] Replacement log: TikTok + Full text are boxed;
- [ ] Replacement log: Close is a flat text action below;
- [ ] Diagnostics uses the same hierarchy;
- [ ] Pending detail uses the same hierarchy;
- [ ] legacy candidate/history detail does not show three cramped equal boxes.

## Quota
- [ ] Google Cloud full width;
- [ ] Queue + Close below;
- [ ] Google Cloud stays on one line.

## Service screen
- [ ] More → Service opens a full screen, not a popup;
- [ ] top/back navigation works;
- [ ] cache/quota summary is visible;
- [ ] Quick Start works;
- [ ] Privacy works;
- [ ] Diagnostics works;
- [ ] Share Diagnostics works;
- [ ] Save Diagnostics works;
- [ ] SearchCache works;
- [ ] Google Cloud works;
- [ ] About works.

## Audits
- [ ] mainactivity-audit PASS;
- [ ] ui-chrome-audit PASS;
- [ ] dialog-style-audit PASS;
- [ ] button-layout-audit PASS;
- [ ] compact-review-audit PASS;
- [ ] action-hierarchy-audit PASS;
- [ ] release-preflight PASS.
