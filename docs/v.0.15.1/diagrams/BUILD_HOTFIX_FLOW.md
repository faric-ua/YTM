# v0.15.1 — Build hotfix flow

```mermaid
flowchart LR
    SDK[Android SDK OK]
    --> RES[Resources OK]
    --> MANIFEST[Manifest OK]
    --> SIGN[Signing validation OK]
    --> KOTLIN[compileReleaseKotlin]

    KOTLIN -->|v0.15.0| FAIL[BuildConfig unresolved]
    KOTLIN -->|v0.15.1| BC[Generated BuildConfig]
    BC --> NEXT[Continue release build]
```
