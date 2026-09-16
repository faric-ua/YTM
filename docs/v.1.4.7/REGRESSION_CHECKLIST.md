# YTM Importer v1.4.7 — Regression checklist

## Upgrade
- [ ] Install over v1.4.6 without uninstall.
- [ ] Current workspace/History/Data/Queue preserved.

## Problem tracks
- [ ] Each problem track is a separate card.
- [ ] Manual replacement shows selected title/channel.
- [ ] Skipped/missing/failed states are readable.
- [ ] TikTok action is full-width.
- [ ] Full text action is full-width below TikTok.
- [ ] Close is a separate flat action below both.

## Service navigation
- [ ] Main → More → Service opens Service home.
- [ ] Service → Quick Start → Back returns to Service home.
- [ ] Service → Privacy → Back returns to Service home.
- [ ] Service → Diagnostics → Back returns to Service home.
- [ ] Service → SearchCache → Back returns to Service home.
- [ ] Service → About → Back returns to Service home.
- [ ] Back from Service home returns to Main.
- [ ] Google Cloud opens browser and returning keeps Service screen.

## Diagnostics
- [ ] No clipped title.
- [ ] Account/current import/quota/cache/local data/privacy are separate cards.
- [ ] Save Diagnostics TXT works without leaving Service.
- [ ] Share Diagnostics TXT works without leaving Service.

## SearchCache
- [ ] Stats are structured in cards.
- [ ] Delete expired works and refreshes screen.
- [ ] Clear all confirmation works.
- [ ] Back returns to Service.

## About / Quick Start / Privacy
- [ ] Long text is split into readable cards.
- [ ] No text is clipped at the top.
- [ ] About → Quick Start and Privacy navigation stays inside Service.

## Audits
- [ ] mainactivity-audit PASS.
- [ ] ui-chrome-audit PASS.
- [ ] dialog-style-audit PASS.
- [ ] button-layout-audit PASS.
- [ ] compact-review-audit PASS.
- [ ] action-hierarchy-audit PASS.
- [ ] service-navigation-audit PASS.
- [ ] release-preflight PASS.
