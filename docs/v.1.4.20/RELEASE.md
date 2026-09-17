# YTM Importer v1.4.20 — Import Action Button Layout Fix

## Scope

Small UI patch after the successful v1.4.19 bulk-export phone test.

## Changes

- Account-import action buttons are no longer forced into a fixed 54dp height.
- Buttons use `WRAP_CONTENT` with a comfortable minimum height.
- Two-line labels remain fully visible.
- The bulk-export action gets a 10dp vertical gap from the single-playlist action.
- Button text can autosize within a controlled readable range.

## Functional scope

No bulk-export data-flow change is intended.

v1.4.19 bulk export remains:

- read-only against source YouTube/YTM playlists;
- folder based;
- one YTM Project per non-empty accessible playlist;
- manifest based;
- round-trip import compatible.

## Version

- versionCode: **54**
- versionName: **1.4.20**

## Test status

**NOT PHONE-TESTED YET**

Required phone check: short Import-screen UI smoke plus one bulk-export start/result smoke.
