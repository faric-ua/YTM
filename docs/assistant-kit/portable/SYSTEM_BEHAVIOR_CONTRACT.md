
# Portable System Behavior Contract

This contract is intended to be copied into another Android project and adapted
to that project's names.

It captures system-level behavior that should remain stable while individual
features evolve.

## 1. Activity recreation is not a user action

Rotation, configuration changes and Android Activity recreation must never be
interpreted as a request to perform an operation again.

Recreation may restore UI state.

Recreation must not automatically:

- submit a form;
- delete an object;
- start a remote write;
- repeat an API mutation;
- start an installer;
- restart a user-confirmed destructive operation.

## 2. Semantic state survives recreation

When practical, preserve the semantic state the user can see or is editing:

- current screen/mode;
- selected entity;
- search/filter query;
- form draft;
- privacy/radio selection;
- opened Help window;
- opened action menu;
- opened confirmation;
- opened result window;
- progress state;
- scroll position when it materially affects continuity.

Restoration means "show the same state", not "execute the same action again".

## 3. Modal lifecycle

Every modal must have a clear owner.

If a modal is visible during recreation:

- restore it over the same semantic parent screen;
- restore its draft/selection where applicable;
- do not fire its positive action automatically;
- Cancel/dismiss must leave the underlying object unchanged unless the action
  had already completed before recreation.

A result modal and the underlying completed status screen are separate states.
Closing a result modal should not silently destroy the completed status screen
unless that is the explicit product contract.

### Shared restorable-modal implementation rule

A visible `Dialog` object is transient UI and must not itself be treated as the
durable state.

For reusable modal lifecycle code:

- persist a semantic modal id plus only the primitive/state payload needed to
  rebuild that modal;
- let a shared controller own save/restore/detach/dismiss bookkeeping;
- let the Activity provide the renderer and domain callbacks for each semantic
  modal id;
- on recreation, rebuild the modal only; never invoke its positive callback;
- detach listeners from the old Activity instance before destruction;
- Cancel/Back/dismiss clears modal state but must not execute the domain action;
- prepared destructive operations may keep their validated input in a safe
  local cache while the semantic confirmation is open.

YTM Importer implements this pattern through `RestorableModalController`.

## 4. Navigation ownership

A screen must know who owns its Back destination.

The same child screen may have different navigation owners depending on where
it was opened from. Do not infer Back ownership solely from Activity class.

Persist or pass the navigation origin when the route requires it.

Test separately:

- toolbar Back;
- Android system Back;
- Cancel;
- Close;
- delegated child flows;
- rotation before Back;
- rotation while a modal is open.

## 5. Remote-operation ownership

Long-running or remote work must not be owned only by a transient Activity
instance.

The operation owner should expose state that a recreated screen can reattach to.

Required invariant:

`recreate UI -> observe existing operation`

not:

`recreate UI -> start another operation`

Mutating remote operations should be idempotent where practical or otherwise
protected from accidental duplicate execution.

## 6. Progress and result lifecycle

Progress has explicit phases such as:

- idle;
- preparing;
- running;
- completed;
- failed;
- cancelled.

Completed operation state should remain inspectable until an explicit user
navigation action dismisses/leaves it.

A summary/result modal may appear over completed state but does not become the
owner of that completed state.

## 7. Forms, text and IME

Editable fields must preserve unsaved drafts through Activity recreation.

Long text fields should not hide content merely to preserve a fixed one-line
layout when multiline input is valid.

Keyboard/IME appearance must not:

- cover required confirmation controls;
- reset a form;
- move the user to another screen;
- submit automatically.

## 8. Lists, filters and search

When a user is working in a list:

- preserve the active filter/query across recreation;
- do not auto-select an item after recreation;
- preserve meaningful selection state;
- keep item actions separate from the item's primary tap behavior;
- destructive item actions require explicit confirmation.

## 9. Destructive actions

Destructive actions require an explicit confirmation boundary.

Long press must never secretly perform the destructive operation itself.

Rotation of an open destructive confirmation restores the confirmation and
must not perform the operation.

## 10. System pickers and external UI

Entering Android/system UI creates a clear ownership boundary.

When external UI is cancelled:

- return to the correct parent;
- preserve prior local state;
- do not treat cancellation as success.

Persisted SAF permissions and direct filesystem permissions are capabilities,
not assumptions.

## 11. Themes and skins

Theme/skin changes may change visual presentation but must not change business
semantics.

Semantic states such as success, warning, danger, duplicate and disabled remain
distinguishable in every skin.

Rotation or theme recreation must not rerun domain actions.

## 12. Application updater

Checking for an update is a read operation.
Downloading is a resumable operation.
Installing is an explicit user/system action.

Activity recreation must not trigger a second download or installer launch.

Before installation:

- verify version identity;
- verify expected package-source contract;
- verify downloaded APK hash;
- use the platform installer;
- never claim silent installation when Android requires confirmation.

## 13. Evidence rule

Static audits prove source contracts.
Unit tests prove deterministic code behavior.
Build CI proves compilation/build/signing contracts.
Only real-device testing proves actual phone UI/lifecycle behavior.

Do not silently convert one evidence class into another.
