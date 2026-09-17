# YTM Importer v1.4.17 — QA Test Run

## Scope

Release: **v1.4.17**

Primary scenario: **FAST_FLOW → existing target playlist → all imported tracks already exist**.

The run validates:

- cache-first fast flow;
- transition from Review to Destination without unexpected navigation to Home;
- duplicate scan for an existing YouTube/YTM playlist;
- preservation of duplicate-scan state during device rotation;
- all-duplicate final action (`Skip duplicates and add`);
- result modal for `Added: 0`.

## Test environment

- App build: **v1.4.17**
- Target: **YTM QA Existing Target**
- Target visibility: **Private**
- Target track count before final action: **6**
- Imported tracks: **3**
- Device orientation sequence during test: **portrait → landscape → portrait**

## Preconditions

1. Three imported tracks are already resolved and ready for write.
2. The existing target playlist already contains all three target YouTube video IDs.
3. The target playlist contains six items before the duplicate check.
4. The FAST_FLOW cache path is available.

## Execution

| Step | Action | Expected | Observed | Result |
|---|---|---|---|---|
| 1 | Open Review in FAST_FLOW | 3 imported, 3 ready, 0 need review | `3 / 3 / 0` | PASS |
| 2 | Continue to existing target playlist | Destination/review state stays intact | Existing target shown with 6 tracks | PASS |
| 3 | Run duplicate scan | 3 selected, 3 already exist, 0 import duplicates, 0 new | `3 / 3 / 0 / 0` | PASS |
| 4 | Observe API counter | Duplicate scan should not fan out into repeated playlist requests | `playlistItems.list = 1 request(s)` | PASS |
| 5 | Rotate portrait → landscape | Screen remains on duplicate check | State remains on duplicate check | PASS |
| 6 | Rotate landscape → portrait | State restored, no navigation to Home | `3 / 3 / 0 / 0`, same screen | PASS |
| 7 | Tap `Skip duplicates and add` | No duplicate items are inserted | Result modal opens | PASS |
| 8 | Inspect result modal | `Added: 0`, existing target updated | `Додано: 0`, existing YouTube/YTM target updated | PASS |

## Final result

**PASS**

No regression was reproduced in the tested FAST_FLOW / existing-target / all-duplicate path.

## Evidence

- `evidence/EVIDENCE_01_ROTATION_STATE_PRESERVED_BLURRED.jpg`
- `evidence/EVIDENCE_02_RESULT_MODAL_ADDED_0.jpg`
- `evidence/EVIDENCE_03_ROTATION_TEST_BLURRED.mp4`
