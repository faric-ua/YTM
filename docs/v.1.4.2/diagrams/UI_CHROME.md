# v1.4.2 — UI chrome

```mermaid
flowchart TD
    A[Activity root] --> B[UiChrome.applyScreenInsets]
    B --> C[systemBars.top + extraTop]
    B --> D[systemBars.bottom + extraBottom]

    E[More / Import / Project actions]
    --> F[UiChrome.showMenuDialog]
    --> G[Dark card menu]
```
