# v1.4.51 — URL Source Matrix

Wave 1 defines only parsing, canonicalization and source classification.
It performs no YouTube API request, no HTML/page scraping, no local playlist
mutation and no remote write.

## Accepted hosts

Exact host allowlist:

- `youtube.com`
- `www.youtube.com`
- `m.youtube.com`
- `music.youtube.com`
- `youtu.be`
- `www.youtu.be`

A suffix lookalike such as `youtube.com.example.org` is not accepted.

`http` and `https` input are accepted because the parser extracts identifiers
only; canonical output is always `https`. A missing scheme is accepted only when
the input begins with an exact allowlisted host.

## Accepted URL forms

| Surface | Form | Required | Result |
|---|---|---|---|
| YouTube | `/playlist?list=<id>` | one non-blank `list` | playlist/Mix candidate |
| YouTube | `/watch?v=<videoId>&list=<id>` | one non-blank `list` | playlist/Mix candidate + exact context videoId when valid |
| YouTube | `youtu.be/<videoId>?list=<id>` | valid 11-char videoId + one `list` | playlist/Mix candidate + context videoId |
| YouTube | `/shorts/<videoId>?list=<id>` | one `list` | playlist/Mix candidate |
| YouTube | `/live/<videoId>?list=<id>` | one `list` | playlist/Mix candidate |
| YouTube | `/embed/videoseries?list=<id>` | one `list` | playlist/Mix candidate |
| YouTube Music | `/playlist?list=<id>` | one non-blank `list` | playlist/Mix candidate |
| YouTube Music | `/watch?v=<videoId>&list=<id>` | one non-blank `list` | playlist/Mix candidate + exact context videoId when valid |

Tracking/query values such as `si` and `playnext` are not part of source
identity and are removed from the canonical URL.

## Classification

Parser-level rule for v1.4.51 Wave 1:

- `list` IDs beginning with exact uppercase `RD` are classified as
  `DYNAMIC_MIX`;
- every other syntactically valid `list` ID is classified as
  `CONCRETE_PLAYLIST`.

`DYNAMIC_MIX` is deliberately a **candidate classification**, not a claim that
the current supported resolver can enumerate it. The resolver wave must still
prove capability. If reliable enumeration is unavailable, it must fail clearly
instead of scraping, guessing or claiming completeness.

## Rejected input

The parser rejects:

- blank input;
- malformed URLs;
- schemes other than `http`/`https`;
- non-allowlisted hosts;
- unsupported paths;
- direct video URLs with no `list`;
- missing/blank `list`;
- multiple different `list` values;
- playlist IDs outside `[A-Za-z0-9_-]` or outside the supported length bound.

The parser never converts a video-only URL into a search request.

## Canonicalization

A supported source produces:

- exact extracted `playlistId`;
- surface: YouTube or YouTube Music;
- source kind: concrete playlist or dynamic Mix candidate;
- optional exact 11-character context `videoId`;
- canonical URL:
  - `https://www.youtube.com/playlist?list=<id>`, or
  - `https://music.youtube.com/playlist?list=<id>`.

Canonicalization does not resolve tracks and does not imply that the source is
readable through the YouTube Data API.

## Resolver boundary

Wave 2 may use the existing YouTube API layer for supported read operations.
A concrete playlist ID can be passed to a resolver only after this parser
succeeds. A dynamic Mix candidate must not be silently treated as an ordinary
durable playlist merely because it contains a `list` value.
