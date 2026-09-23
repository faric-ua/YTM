# v1.4.51 — URL/Mix Snapshot Contract

## Source classes

### Concrete playlist

A URL that identifies a concrete playlist is expected to map to one ordered
remote playlist state that can be read at import time.

The local snapshot preserves the ordered resolved items from that read.

### Dynamic Mix / radio source

A Mix/radio-style URL may represent a recommendation session rather than a
durable playlist object.

For such a source:
- detect it explicitly;
- never describe it as a durable remote playlist;
- capture only the current resolved set when technically supported;
- make the snapshot boundary visible to the user;
- fail clearly when reliable enumeration is unavailable.

## Wave 1 parser classification

The authoritative accepted-form matrix is `URL_SOURCE_MATRIX.md`.

Wave 1 classification is intentionally syntactic:
- exact uppercase `RD` list IDs become `DYNAMIC_MIX` candidates;
- other valid list IDs become `CONCRETE_PLAYLIST` candidates;
- tracking parameters are discarded during canonicalization;
- an exact 11-character context videoId is preserved when present;
- parser success does not claim that the source is readable or complete.

Resolver capability is a separate Wave 2 decision. No parser path may fetch a
page, call the YouTube API, mutate the local playlist or start a remote write.

## Track identity

Priority:
1. exact source videoId when available;
2. explicit unresolved/unavailable state when exact identity cannot be read.

Do not silently replace an unavailable source item with a different search
candidate during URL snapshot import.

Search remains an explicit downstream user workflow.

## Ordering and duplicates

Source order is significant.

Duplicate occurrences are significant and remain separate ordered entries.
The import layer must not deduplicate merely because videoId values repeat.

## Preview / commit boundary

Remote resolution and local commit are separate semantic phases.

Resolve:
- may perform allowed remote reads;
- produces a preview model;
- does not replace the current local playlist.

Commit:
- happens only after explicit confirmation;
- replaces/sets the current local snapshot through existing local workspace
  semantics;
- performs no remote playlist write.

Cancel/Back before commit leaves the current local playlist unchanged.

## Lifecycle

A rotation/recreation may restore:
- entered URL;
- source classification;
- completed preview state;
- error/result state.

A rotation/recreation must not:
- automatically start/restart URL resolution;
- automatically commit a snapshot;
- launch Review/Search/write work.

## Failure semantics

User-visible failure must distinguish at least:
- unsupported/invalid URL;
- source not readable with the available integration;
- authorization failure when authorization is required;
- quota/network/API failure;
- partially unavailable source items.

Partial source data must never be presented as a complete result without an
explicit count/warning.

## Existing-system reuse

Reuse the existing:
- authorization invalidation contract;
- quota tracker for API reads;
- local current-playlist model/store;
- Review/Search downstream flows;
- YTM Project/local export semantics;
- common lifecycle/modal/navigation contracts.

Do not introduce a parallel playlist/write subsystem.
