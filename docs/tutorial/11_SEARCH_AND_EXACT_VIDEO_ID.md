# 11 — Search vs exact videoId

This chapter uses BUG-005 as a concrete lesson in the difference between **text search** and **canonical identifiers**.

## 1. Two ways to know a track

A plain imported track may contain only human-readable text:

`Artist — Title`

That is a search problem. The app may need SearchCache or YouTube `search.list`.

An account export or YTM Project may already contain the exact YouTube `videoId`.

That is not a search problem. The canonical identifier is already known.

## 2. The v1.4.26 failure

The selective-export round trip itself worked:

`account → export → YTM Project → import → exact videoId 3/3`

But the next ordinary `↻ Пошук` action still planned three new `search.list` requests.

Persistence was correct; downstream interpretation was wrong.

This is BUG-005.

## 3. Why selectedVideoId alone is not enough

A searched candidate can also have a `selectedVideoId`.

Therefore the rule cannot simply be:

> if selectedVideoId exists, never search.

YTM Importer distinguishes a canonical exact selection using state:

- `selectedVideoId` is present;
- status is `MATCHED`;
- candidate list is empty.

A candidate-based search result has candidate context and can remain eligible for intentional repeat search.

## 4. Defense in depth

v1.4.27 protects the invariant at more than one layer.

The ordinary UI call path explicitly preserves exact selections.

The normal `searchAll()` default is also exact-preserving.

`SearchCoordinator` owns the domain predicate through one canonical helper and uses the same rule for planning and execution.

This matters because fixing only a button call leaves room for the next caller to reintroduce the bug.

## 5. Quota is part of correctness

YouTube search quota is limited.

A redundant call is not merely inefficient UI behavior. It can consume a scarce resource while providing no new information.

Therefore the test is quantitative:

`exact 3/3 → search required 0 → new search.list 0`

## 6. Real-phone proof

The v1.4.27 phone retest reused the same `top 3` project that reproduced the bug.

Observed:

- Home: 3 tracks, 3 ready/exact, 0 missing;
- Review: 3/3 ready;
- ordinary repeat-search plan: 0 tracks requiring search;
- new `search.list`: 0.

BUG-005 / Q-005 was closed for that targeted path.

## 7. Testing lesson

A round-trip test should not stop at successful serialization.

Continue into the next meaningful user action.

For identifier-rich data, test both:

1. **persistence invariant** — the identifier survives;
2. **behavior invariant** — downstream code uses the identifier and does not redo expensive discovery work.

That distinction is what exposed BUG-005.
