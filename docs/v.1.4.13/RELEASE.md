# YTM Importer v1.4.13 — Cleanup Wave 3 / SearchCoordinator

## Test status

- **v1.4.12: NOT TESTED**
- **v1.4.13: NOT TESTED YET**

Phone-test status is tracked in the mutable root file:

`RELEASE_TEST_STATUS.md`

The immutable `docs/v.1.4.12/` release notes are intentionally not rewritten.

## Goal

Continue reducing `MainActivity` without touching the create/append write
engine yet.

Cleanup Wave 3 extracts the track-search domain into:

`app/src/main/java/com/saney/ytmimporter/search/SearchCoordinator.kt`

## MainActivity size

```text
before v1.4.13: 3689 lines
after  v1.4.13: 3620 lines
```

## SearchCoordinator now owns

- deciding which tracks need search;
- preservation of manual selections;
- preservation of exact YTM Project video IDs;
- SearchCache lookup;
- SearchCache write;
- cache-hit counting;
- YouTube `search.list` calls;
- local search-quota accounting;
- quota-error stop behavior;
- automatic best-candidate application;
- MATCHED / REVIEW / MISSING / FAILED search-state transitions.

## MainActivity still owns

MainActivity no longer contains the search domain loop.

It retains only:

1. open the search-plan confirmation UI;
2. request Google authorization if needed;
3. start work on the existing executor;
4. render search progress to the screen;
5. open ReviewActivity when requested.

So the relationship is now:

```text
MainActivity
    UI + auth bridge
        ↓
SearchCoordinator
    search domain
        ↓
SearchCache + QuotaTracker + YouTubeApi
```

## Behavior intentionally preserved

### Manual choice

A track with:

```text
manuallySelected = true
selectedVideoId != null
```

is not searched again.

### Exact Project video ID

With `preserveExistingExact = true`, a resolved Project track with no candidate
list is preserved exactly.

### Cache

A valid cache hit:

- does not call YouTube search;
- records a local cache hit;
- reuses candidates already rescored by current MatchScorer.

### Quota

After the first quota failure:

- quota error is recorded locally;
- one UI warning callback is emitted;
- cached tracks can still be processed;
- uncached tracks become FAILED with the existing quota message.

### Auto-match threshold

The existing threshold remains:

```text
score >= 0.72 → MATCHED
score < 0.72  → REVIEW
```

## Q-002

Custom-dialog entrance motion remains:

**DEFERRED BY USER**

No UiChrome positioning/animation change is made in this release.

## Version

```text
versionCode = 47
versionName = "1.4.13"
```
