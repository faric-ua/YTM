# YTM Importer — Product Direction and Delivery Strategy

**Owner-approved conversation: 2026-10-09.** Active project repository:
`faric-ua/YTM`, branch `feat/v1.4.55-ux-hardening`.

**Authoritative detail and tracked user ideas:** [GitHub #59](https://github.com/faric-ua/YTM/issues/59).
**Immediate work:** [#27 Bulk preparation UI](https://github.com/faric-ua/YTM/issues/27)
and [#28 stale Bulk status](https://github.com/faric-ua/YTM/issues/28).
**This is a planning document, not an implemented feature or PHONE PASS.**

## User's concrete direction

The user is frustrated by endless micro-corrections, repeated tests, extended
assistant stalls and an ever-expanding list of desirable features. The product
should ultimately make it straightforward for a nontechnical user to:

1. Import playlists in familiar formats or through URL.
2. Review low-confidence/unmatched items, correct them, and copy the original
   track's Artist — Title to clipboard with one tap to find it on YouTube.
3. Keep local collections safely and select playlists to synchronize.
4. Synchronize many chosen playlists through **one understandable entrypoint**
   with preflight, transparent progress, explicit mutation consent, recovery and
   meaningful results. Note: `Меню → Синхронізувати всі` and the underlying
   protected Bulk engine already exist, but consumer UX is still complex.
5. Export/back up with a clear recommended default rather than confusing
   users with 6–7 similar-looking choices.

## Delivery: stop polishing forever

### Stage A — Ship the stable importer, not every imaginable feature

- Finish **#27/#28 together** as one safe Bulk preparation/state cleanup wave.
- Freeze new polish for v1.4.55, perform a short consolidated release acceptance
  only for changed contract owners, and cut the v1.4.55 stable checkpoint.
- **No need** to finish languages, premium skins, auto queue or playback to ship
  a usable core importer. Anything optional can live in the roadmap.
- Existing PHONE-accepted #29/#30/#40/#58 must **not** be repeated unless a
  protected owner changes or new contradictory evidence appears.

### Stage B — The next core product wave

- **One-action sync UI** reusing existing Bulk preflight/session/store/ledger,
  not a second write implementation. Single obvious entrypoint, local playlist
  selection, NEW/LINKED/ALREADY_SYNCED/NEEDS_SEARCH/PENDING/BLOCKED summary,
  quota estimate, preview, explicit start, durable progress, skip unchanged,
  History/recovery, and accurate mixed success results.
- **Safe optional playlist creation queue:** user can enqueue multiple local
  playlists and explicitly authorize processing under account/API limits.
  Durable state, one writer, exact linked playlist identity and idempotent
  operation records, failure-specific pause/cooldown, no automatic duplicate
  creation or blind retries on errors. Preserve existing YTM safe-write/rollback
  invariants. User-proposed ~30-minute spacing is **only a configurable
  preference**, never a documented YouTube-safe interval or anti-limit trick.
  Android background scheduling is inexact and must be disclosed.
- **Review quick wins:** tap-to-copy original track title and separate manual
  result title where useful (explicit `Скопійовано` feedback, accessibility,
  no conflict with opening the track). Fix ambiguous export choice via default
  and advanced options.

### Stage C — Quality of search and richer experience

- **Future Trance Volume 15 matching:** user observed about four
  low-similarity/not-found items despite apparent exact YouTube results found
  manually. Wait for promised screenshot/track identities. Compare local
  metadata normalization, query, candidate set, YouTube API search vs website,
  score/threshold, videoId, result visibility; then fix the real cause.
  Do not assume all four are false negatives or alter score blindly.
- **Localization:** Ukrainian baseline plus English and other requested
  languages, with centralized resources and fallback; no source-string scatter.
- **Distinct, high-quality visual skins:** beyond Neon/Blue/Green recolors,
  while preserving contrast, state meaning, navigation and performance.
- **Optional music/video player library:** product exploration separate from
  the importer MVP and from the independent Dorama.land video project. Prefer
  compliant official playback/embedding and permitted sources; inspect media
  provider policy, DRM restrictions, lifecycle/audio focus, resume/offline rights
  before promising implementation.

## YouTube creation blocking — research and safe response

Official references:
- https://support.google.com/youtube/answer/57792
- https://developers.google.com/youtube/v3/docs/playlists/insert
- https://developers.google.com/youtube/v3/docs/playlistItems/insert
- https://developers.google.com/youtube/v3/getting-started
- https://developers.google.com/youtube/v3/docs/errors

**Confirmed:** YouTube documents a daily cap on *public* playlists per channel
across YouTube, YouTube Music and API, but does **not publish an exact fixed
daily count or cooldown period**. `playlists.insert` costs 50 quota units
per call and `playlistItems.insert` costs 50 per track. The typical general
Data API default is 10,000 units/day; per-method quotas differ and may change.

**Not confirmed:** user's observed 5–6 playlist creations before a temporary
block might reflect public-playlist channel cap, short-window abuse/rate controls,
per-user restrictions, project quota or a different problem. We cannot infer
a safe hourly frequency or "temporary" duration without the exact provider
status, `errors[].reason`, timestamp, request count, playlist privacy and
observed recovery. Never attempt to defeat provider rate limits.

**Future queue contract:** record the failure verbatim (redact tokens),
classify it; pause **all new writes** on quota/rate protection; provide a clear
waiting state and user-approved retry only when provider permits it, respecting
`Retry-After` if present. Do not assume retrying every 30 minutes is safe.
Avoid duplicate create/adds and quota-consuming retry storms. Plan for quota
reset vs unknown account cooldown as different states.

## Process / QA policy — reduce churn while keeping data safe

- **Never implement all remote-writing features roughly and test only at the
  end.** That increases risk of identity, data-loss, duplicate operations and
  uncaught regressions; instead deliver coherent **end-to-end user journeys**.
- Batch related **UI-only** fixes into one wave and one focused PHONE smoke
  rather than one APK per pixel or dialog.
- For each *new protected invariant* extend the appropriate existing shared
  audit/test; avoid adding a parallel audit for every cosmetic change.
- Distinguish SOURCE IMPLEMENTED / CI PASS / SIGNED / PHONE ACCEPTED.
- No automatic Search/create/write/restore/rollback triggered by rotation,
  app restart, a preview, or leaving/returning to a screen.
- Preserve accepted phone tests; only changed owner/new contrary evidence
  justifies revisiting them.
- Agent response format: `docs/assistant-kit/USER_RESPONSE_TEMPLATE.md`
  (short header, progress, Termux at end, one short post-APK test at very end).

## Definition of the importer MVP

A newcomer can find a simple **Import → Review → Save locally → Sync → Result**
path; local/linked playlist identities are trustworthy; writes are opt-in;
YouTube failures/cooldowns never create silent duplicates; backup/recovery
works; advanced options do not overwhelm first-time users.

**Active checkpoint:** #58 CLOSED / PHONE ACCEPTED. Next source work #27/#28.
Future product ideas remain *planned*, not required for v1.4.55 closeout.
