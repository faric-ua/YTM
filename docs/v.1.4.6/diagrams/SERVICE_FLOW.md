# v1.4.6 — Service screen flow

```mermaid
flowchart LR
    M[MainActivity More] --> S[ServiceActivity]
    S --> A[Action result]
    A --> M
    M --> Q[Quick Start / Privacy / Diagnostics / Cache / Cloud / About]
```
