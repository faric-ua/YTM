# v1.4.55 — #40 Review manual URL / manual-choice presentation

**SOURCE CANDIDATE — Validate Android and PHONE acceptance pending.**

Review track → `Вставити YouTube / YTM URL` opens the same native
AlertDialog with a theme-aware 2–3-line wrapping URI input, clear × labeled
`Очистити URL`, and fixed native Cancel / Use buttons.
Long text is inspectable. Clear only changes the local draft and cannot
trigger a lookup; the existing positive `Використати` action alone calls
`extractVideoId` and `ReviewRemoteOperations.startManualLookup`.

Retain existing `STATE_MANUAL_URL_DIALOG_OPEN`, `STATE_MANUAL_URL_DRAFT`,
`STATE_MANUAL_URL_HISTORY_INDEX`, focus, and track restoration on rotation;
recreating UI must not automatically start any API calls, Search or writing.
Cancel/Back returns to the same Review track.

For a manually chosen **match**, the Review list/detail now display a separate
theme-accent `✓ Ручний вибір` label and prominent selected replacement,
while the original title remains separate. Pure `ReviewManualPresentation`
must exclude manual SKIPPED tracks with no selected title.
Recycled rows reset label visibility, color and text weight. Semantic status
is text-plus-shape; no hardcoded orange/green.
No Search/Bulk/History/YTM remote/domain data owner changes.

FOCUSED PHONE QA PENDING:
1. Long manual URL wraps and can be inspected; × clear with no lookup.
2. Portrait→landscape→portrait restores the same dialog/draft/track.
3. Cancel/Back does not submit or change selected track.
4. After an explicit manual match, Review list and detail distinguish it
   from automatic matches; manual skip must not show successful-choice label.
5. Check Neon and an alternate theme for legible manual status.
Do not repeat accepted #29/#30 tests.
