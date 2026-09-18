# YTM Importer v1.4.40 — In-app Release History

- versionName: **1.4.40**
- versionCode: **76**
- status: **NOT PHONE-TESTED YET**
- focus: **About → Release History**

## User-visible change

The `Про YTM Importer` page now includes a third card under `Дізнатися більше`:

- `Швидкий старт`
- `Приватність`
- `Історія змін`

`Історія змін` opens a dedicated full-screen page with the application's release history.

## Single source of truth

The app does not maintain a second manually copied changelog.

During Android build, Gradle copies the repository root:

`CHANGELOG.md`

into generated app assets.

`ServiceActivity` reads that embedded file at runtime and renders its `## release` sections as cards.

This keeps repository history and in-app history synchronized.

## Presentation

- fixed Back/title header;
- normal scrollable content;
- newest releases first;
- one card per release;
- Markdown bullets are shown as readable bullets;
- lightweight back navigation returns from `Історія змін` to `Про YTM Importer`, not directly to Service home.

## Boundaries

- no network access;
- no remote changelog fetch;
- no new permission;
- no analytics;
- v1.4.39 History JSON restore remains unchanged and still needs phone QA.
