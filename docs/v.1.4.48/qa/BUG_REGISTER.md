
# v1.4.48 — Bug / UX Register

## UX-023 — Playlist Editor Long Title Visibility

Status:

**CLOSED — PHONE RETEST PASS v1.4.48**

Observed on the first v1.4.48 functional QA build:

- playlist title input was single-line;
- long titles were not fully visible.

Fix:

- multiline input;
- minimum two visible lines;
- wrapping instead of horizontal single-line scrolling;
- height grows with content.

Phone retest:

`5+` on signed run `35673239632`.

## Other release-specific findings

No additional v1.4.48 playlist-management finding remains open from the
targeted phone run.

Global/deferred issues remain in root/global bug registers and are not
implicitly closed here.
