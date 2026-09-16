# v1.4.12 — MainActivity before / after

```text
BEFORE — 5668 lines

MainActivity
├── Home
├── Import UI + parser bridge
├── Review/candidate dialogs
├── Auth
├── Search
├── Destination orchestration
├── Write core
├── Pending list/detail/resume
├── Quota
├── Service popups
├── Diagnostics
├── SearchCache popup
├── Data / export / backup / restore
├── History list/detail/actions
└── Result/replacement UI


AFTER — 3689 lines

MainActivity
├── Home/navigation
├── Auth
├── Search orchestration
├── Destination bridge
├── Write core
├── Pending resume bridge
├── Quota
├── History sync
├── Manual URL result application
└── Result/replacement entry point

Dedicated screens
├── ImportActivity
├── ReviewActivity
├── DestinationActivity
├── PendingActivity
├── HistoryActivity
├── DataActivity
└── ServiceActivity
```
