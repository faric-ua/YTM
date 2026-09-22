# v1.4.49 — Updater Flow

```mermaid
flowchart TD
    A["[Головна]"] --> B["Меню"]
    B --> C["Сервіс"]
    C --> D["Про YTM Importer"]
    D --> E["Версія"]
    E --> F["Перевірити оновлення"]
    F --> G["Checking"]

    G --> H{"Manifest valid?"}
    H -- "No" --> X["Error: malformed / unsupported"]
    H -- "Yes" --> I{"remote versionCode > installed?"}

    I -- "No" --> J["Up to date"]
    I -- "Yes" --> K["Update available"]

    K --> L["Explicit Download"]
    L --> M["Downloading"]
    M --> N["Verifying SHA-256"]

    N --> O{"SHA matches?"}
    O -- "No" --> Y["Error: verification failed"]
    O -- "Yes" --> P["Ready to install"]

    P --> Q["Explicit Install"]
    Q --> R["Android package installer"]
    R --> S{"User result"}
    S -- "Cancel" --> T["Return safely to updater"]
    S -- "Installed" --> U["Relaunch / verify installed version"]

    G -. "rotation/recreation" .-> G
    M -. "rotation/recreation: reattach, do not duplicate" .-> M
    N -. "rotation/recreation" .-> N
    P -. "rotation/recreation: do not auto-launch installer" .-> P
```

## Wave status

Wave 1 implements the route through `Up to date` / `Update available` / `Error`.
The Download → Verify → Install branch remains planned and is not auto-entered.

## Version relation contract

- remote `versionCode` == installed → Up to date;
- remote `versionCode` < installed → installed build is newer; informational
  no-update state, never Error and never downgrade;
- remote `versionCode` > installed → Update available.

## Lifecycle contract

Recreation restores updater state but is never treated as another Check,
Download or Install command.
