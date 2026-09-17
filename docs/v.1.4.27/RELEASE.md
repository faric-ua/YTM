# YTM Importer v1.4.27 — Exact-ID Search Guard

## Goal

Fix BUG-005 discovered during the v1.4.26 selective-export round trip.

A YTM Project can already contain canonical exact YouTube `videoId` values.
Ordinary/manual repeat-search must not spend `search.list` quota on those exact
tracks.

## Behavior

For the ordinary Search / repeat-search path:

- explicit manual selections remain protected;
- project/account exact selections remain protected;
- exact tracks with empty candidate lists are not re-searched;
- non-exact tracks can still use SearchCache / YouTube search;
- previously searched candidate-based matches can still be intentionally
  refreshed by the existing repeat-search path.

The important distinction is:

> exact imported/project selection != ordinary search candidate.

## UI clarification

The Review `↻ Пошук` confirmation no longer claims that every track will be sent
through search. It explains that exact `videoId` tracks are preserved and only
tracks that still need search are processed.

## Version

- versionCode: **61**
- versionName: **1.4.27**

## Status

**NOT PHONE-TESTED YET**

Phone retest target:

- reopen/import the v1.4.26 `top 3` project;
- verify exact videoId = 3/3;
- press Review `↻ Пошук`;
- expected search plan: **0 tracks to search / 0 new search.list**;
- Review must remain 3/3 ready.
