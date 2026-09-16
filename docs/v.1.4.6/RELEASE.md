# YTM Importer v1.4.6 — Action Hierarchy + Service Screen

## Why

The UI now has several kinds of dialogs. Treating every action as the same
boxed button created cramped rows and made confirmation dialogs look like menus.

v1.4.6 defines a simple hierarchy.

## Dialog rule

### Confirmation dialogs — keep simple text actions

Dialogs such as:

- Repeat search;
- Save full backup;
- Share full backup;
- Restore backup;
- delete confirmation;

have only `Cancel + Confirm` and intentionally remain compact text actions at
the bottom of the dialog.

These are confirmations, not menus.

### Three-action information dialogs

For dialogs such as diagnostics, replacement log, candidate detail, history
legacy detail and pending detail:

```text
[ primary action ][ secondary action ]
          Close / Back
```

The first two actions remain boxed. The trailing `Close`, `Back` or `Not now`
is rendered as a flat accent text action across the full width.

This prevents labels such as `Закрити` from wrapping inside a narrow third
button.

### Quota dialog

Quota keeps its special hierarchy from v1.4.5:

```text
[          Google Cloud          ]
[       Queue       ][   Close   ]
```

## Replacement / problem tracks

Actions are now explicit:

- `TikTok список`
- `Повний текст`
- flat `Закрити`

## Service

The former `Сервіс` popup was too large and visually still used a plain Android
list.

It is now a dedicated screen:

`ServiceActivity.kt`

The screen contains styled cards grouped into:

- Help;
- Diagnostics;
- API and local data;
- About.

It also shows the current SearchCache count and local search-quota summary.

The actual service operations still run through the existing MainActivity core
via an Activity result contract.

## Version

```text
versionCode = 40
versionName = "1.4.6"
```

## Q-001

Q-001 remains OPEN.
