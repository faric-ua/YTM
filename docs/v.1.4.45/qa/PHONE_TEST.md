# v1.4.45 Phone QA — Unified Window Title Emphasis

## A — Full-screen title

Open at least two screens, for example:
- `Меню`;
- `Квота API`;
- `Імпорт` or `Історія`.

Expected:
- the page title is visually stronger than body/subtitle text;
- the title uses the current theme's accent color;
- text remains readable and does not clip.

## B — Dialog title

Open a representative modal such as `Підтвердити Restore`, `План пошуку`, or a
help/result modal.

Expected:
- the modal title uses the same theme-aware emphasis;
- body text remains visually secondary;
- buttons/actions are unchanged.

## C — Theme smoke

Check at least two themes; ideally Neon plus Blue or Green.

Expected:
- title color follows that theme's accent;
- no Home workflow-state semantic colors are changed by this release.

## D — Rotation/navigation smoke

Rotate one full-screen page and one modal.

Expected:
- title remains readable;
- Back/Close still work;
- no action fires because of rotation.

## Real-phone result — 2026-09-19

**PASS.**

Confirmed:
- full-screen title emphasis: PASS;
- representative dialog title emphasis: PASS;
- Neon + alternate-theme accent behavior: PASS;
- rotation/navigation smoke: PASS.

During the same phone session, Google OAuth showed HTTP 403 `access_denied` for a
non-approved account because the OAuth app audience is still in Testing. This is an
OAuth console audience/test-user configuration issue, not a UX-022 regression.

Status: **PHONE RETEST PASS — UX-022 CLOSED**.
