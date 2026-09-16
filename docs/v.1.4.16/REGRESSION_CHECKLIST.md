# v1.4.16 regression checklist

Status before phone run: **NOT TESTED YET**

Required release-focus cases:
- [ ] G-04 Existing playlist list loads and intended target can be selected.
- [ ] G-05 Existing playlist duplicate scan detects remote + incoming duplicates.
- [ ] G-06 Skip duplicates writes only new tracks and marks skipped duplicates locally.
- [ ] G-07 Add duplicates anyway honors explicit user choice.
- [ ] Existing-playlist scan failure still offers the existing no-scan path.
- [ ] Back navigation Start ↔ Existing list ↔ Confirm preserves the flow.
- [ ] H-01/H-04 result modal smoke test after append.
- [ ] I-01/I-03 Pending Queue smoke test if quota interruption can be reproduced safely.

Core smoke regression:
- [ ] Import a small test list.
- [ ] Google/YTM account can authorize manually.
- [ ] Search/review still works.
- [ ] Create a new private playlist.
- [ ] Append to an existing playlist.
- [ ] No crash on portrait → landscape → portrait during destination flow.

Known non-blocking/deferred for this cleanup wave:
- BUG-003/Q-003 silent auth recovery after in-place update remains deferred.

Do not mark v1.4.16 PHONE TESTED until the required cases above are actually run on a real phone.
