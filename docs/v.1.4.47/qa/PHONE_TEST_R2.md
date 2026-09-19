# v1.4.47-R2 Phone QA

Install over v1.4.47-R1 without uninstalling or clearing app data.

## 1 — Portrait Home density

Expected:
- version shows v1.4.47-R2;
- Neon/Blue/Green visual language is unchanged;
- current playlist heading is inside the playlist card;
- quick actions are inside one compact section container;
- heading-to-content gaps are visibly tighter;
- bottom navigation has rounded outer corners;
- lower Home content fits better before scrolling than R1.

## 2 — Landscape Home

Rotate Home to landscape.

Expected:
- dashboard remains scrollable;
- current playlist and quick actions remain reachable;
- bottom navigation stays fixed and rounded;
- no clipping/overlap blocks taps.

## 3 — Theme picker parent

Path:
`Home → Меню → Тема`

Expected:
- MenuActivity stays visible behind the theme picker;
- picker is not shown over Home;
- selecting Green or Blue recreates/repaints MenuActivity;
- after selection, Menu remains the current page;
- Back returns from Menu to Home normally.

## 4 — Current playlist / quick actions smoke

Expected:
- current playlist card still opens Playlist Hub;
- Import quick action opens Import;
- Export playlists quick action still opens the existing Import/export surface;
- no duplicated external `Поточний плейлист` / `Швидкі дії` headings remain.

## 5 — Copy cleanup

With no target playlist ID, trigger either Open-in-YTM or Copy-link fallback.

Expected:
`Створіть / виберіть плейлист`

Old long copy must not appear.

## 6 — R1 regression smoke

Quickly spot-check:
- Playlist Hub problem dialog still remains Hub-owned;
- Search/Create Back returns to Hub;
- replacement dialog still survives rotation;
- modal colors still follow Green/Blue;
- destructive actions remain red/danger.

Report format:
`1+ 2+ 3+ 4+ 5+ 6+`

Status: **PHONE QA NEEDED**.
