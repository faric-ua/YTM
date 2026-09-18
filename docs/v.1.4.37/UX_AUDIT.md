# v1.4.37 UX architecture audit

## 1. Fixed-header / fixed-footer storage screen

`StorageChooserActivity` owns the long-list storage UX.

Structure:

```text
fixed top bar:    Back | title | ?
scroll middle:    remembered SAF roots
fixed footer:     Add folder
                  [System save / rename in SAVE mode]
                  Cancel
```

Only the middle root list scrolls.

## 2. Modes

### TREE

Used by the seven Import folder operations.

A remembered root returns a tree URI to the calling Import request code.

`Додати іншу папку…` opens `ACTION_OPEN_DOCUMENT_TREE` inside the chooser. After a grant, the chooser persists access and returns the new tree URI.

### SAVE

Used by Data / Review / History / Service.

A remembered write root returns `RESULT_TREE` for direct `SafTreeFileWriter` output.

`Додати папку для швидкого збереження…` grants a new write root and returns it.

`Системне збереження / змінити ім’я…` opens the only active `ACTION_CREATE_DOCUMENT` path and returns `RESULT_DOCUMENT`.

## 3. System picker escape behavior

The system picker is launched by `StorageChooserActivity`, not directly by the business screen.

If the user presses Android Back inside the system picker, Android returns to `StorageChooserActivity`. The chooser's fixed Back/Cancel controls are still immediately available.

## 4. Utility screens

`QuotaActivity` reads current local quota and pending-job state directly and presents a dedicated full-screen page.

Its Queue action returns to MainActivity, which opens `PendingActivity`; this preserves the existing Pending resume/result contract.

`MenuActivity` is a full-screen list of utility actions. It returns a semantic action to MainActivity so existing project/theme/service behavior stays centralized.

Home label:

- old: `Ще`
- new: `Меню`

## 5. Explicit non-goals

v1.4.37 does not:

- solve the two `ACTION_OPEN_DOCUMENT` file-open flows yet;
- change Neon Dark Home colors;
- close UX-009;
- add broad storage permissions.
