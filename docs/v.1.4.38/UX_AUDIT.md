# v1.4.38 UX audit

## 1. Selector rule

Use a dedicated full-screen selector when the user is choosing from a dynamic/long collection.

Current migrated families:

1. connected-account YTM playlist import;
2. selective playlist export;
3. delta-chain head/session selection;
4. backup/manifest project selection.

Compact informational or confirmation content may stay in UiChrome modals.

## 2. Full-screen selector architecture

`ListSelectorActivity` receives display labels and opaque serialized values.

It returns only the selected values to the caller.

This keeps business-domain decoding in `ImportActivity` while centralizing the UI pattern.

Layout:

```text
fixed header:       Back | title | ?
fixed summary:      selected count / prompt
scroll middle:      selectable items
fixed footer:       Confirm | Cancel
```

## 3. Selector result safety

The caller serializes enough domain data to reconstruct the selected item after the selector closes:

- `YouTubePlaylistInfo`;
- `DeltaChainHead` + root tree URI;
- `AccountLibraryManifestEntry`.

No network write action is owned by the selector.

## 4. Destructive confirmation rule

Destructive local mutations use a dedicated danger confirmation.

Confirmation copy must answer:

- what exactly will be removed;
- whether YouTube/YTM remote data changes;
- whether a backup/safety path exists;
- what recovery becomes impossible.

Bulk destruction should be described more strongly than deleting one local item.

## 5. Restore safety snapshot

Snapshot deletion is deliberately separated from rollback success.

Success acknowledgement is not the place for a destructive side action.

The Data screen owns the explicit snapshot-delete entry point, which then opens a danger confirmation.

## 6. Mobile action labels

Primary mobile buttons should communicate one action and fit on one line whenever practical.

Long explanations belong in surrounding copy, not inside button labels.

## 7. Non-goals

- no broad storage permissions;
- no Neon Dark recolor;
- no UX-009 Green Dark state-color fix yet;
- no replacement of the two generic open-document flows yet.
