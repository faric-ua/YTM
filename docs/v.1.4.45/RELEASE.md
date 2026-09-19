# v1.4.45 — Unified Window Title Emphasis

## Scope

UX-022 strengthens the first/title line inside dialogs, modal windows, and full-screen
utility/detail screens.

## Implementation

- adds shared `UiChrome.emphasizedTitle(...)`;
- the shared title uses the active theme's accent color plus bold type;
- UiChrome dialog headers use the same shared emphasized title path;
- top-bar titles migrated across Import, Review, History, Queue, Destination, Service,
  Data, Menu, Quota, ListSelector, StorageChooser, and RecentFileChooser;
- existing font-size and line-count choices are preserved per screen;
- body copy, action semantics, workflow-state colors, and Neon state semantics are not
  changed.

## Version

- versionName: **1.4.45**
- versionCode: **85**

Status: **PHONE RETEST PASS — UX-022 CLOSED**.
