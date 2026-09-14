# YTM Importer — Versioned documentation

Документація зберігається окремо для кожної збірки/релізу.

```text
docs/
├── README.md
├── v.0.7.1/
│   ├── RELEASE.md
│   └── diagrams/
│       ├── README.md
│       ├── CURRENT_FLOW.md
│       ├── ARCHITECTURE.md
│       ├── TRACK_STATE_FLOW.md
│       └── BEFORE_AFTER_v0.7.1.md
│
└── v.0.8.0/
    ├── RELEASE.md
    └── diagrams/
        ├── README.md
        ├── CURRENT_FLOW.md
        ├── ARCHITECTURE.md
        ├── TRACK_STATE_FLOW.md
        └── BEFORE_AFTER_v0.8.0.md
```

## Правило на майбутнє

Для кожної нової версії створюється окрема папка:

`docs/v.X.Y.Z/`

Наприклад:

`docs/v.0.9.0/diagrams/`

Попередні версії не перезаписуються. Це дозволяє швидко відкрити GitHub і побачити логіку програми саме для потрібної збірки.

GitHub автоматично рендерить `mermaid` блоки всередині `.md` файлів у справжні діаграми.
