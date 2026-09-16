# YTM Importer — Test data guide

## SMALL-3
Use for every development build.

Recommended pattern:
1. one very well-known official track;
2. one track likely to have several versions/remixes;
3. one track that you will replace manually by URL.

Purpose:
- fast import;
- search;
- candidate review;
- manual selection preservation;
- create result modal.

## CACHE-REPEAT
Use exactly the same SMALL-3 list twice.

Purpose:
- verify SearchCache hit behavior;
- verify second run avoids unnecessary search.list quota.

## INVALID
Prepare:
- empty TXT;
- malformed CSV;
- non-playlist JSON;
- invalid YouTube URL;
- inaccessible/deleted YouTube video URL.

Purpose:
- failure messaging and state preservation.

## PROJECT
Create a YTM Project from a reviewed list containing:
- at least one automatic MATCHED track;
- at least one manual URL track;
- at least one skipped/problem track.

Re-import it and verify exact IDs/provenance.

## DUPLICATES
Prepare an imported list containing:
- one video already present in destination playlist;
- the same video repeated twice inside the imported batch;
- one unique video.

Verify skip/add-duplicates branches.

## STRESS-50
Use the established 50-track Clubland regression set already used by the
project for release stress testing.

Purpose:
- search/cache performance;
- review usability;
- Project save before export;
- long create/append flow;
- History/Queue consistency.
