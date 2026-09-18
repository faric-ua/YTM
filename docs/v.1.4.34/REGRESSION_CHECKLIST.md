# v1.4.34 regression checklist

- [ ] v1433 historical/unified-modal audit
- [ ] v1434 back-navigation audit
- [ ] dialog style/bounds/animation audits
- [ ] full release preflight
- [x] signed APK
- [x] Import back arrow visually centered
- [x] back-arrow spot-check on at least two other secondary screens
- [ ] back button still navigates correctly
- [ ] incremental backup preflight stable
- [x] Home `Квота` stable
- [ ] one legacy builder message/confirm stable
- [ ] selective export multi-choice stable and functional
- [ ] manual-link custom-view stable and functional
- [ ] one menu modal stable

BUG-002 closes only after representative modal categories pass on the phone.

Remaining phone cases were explicitly deferred by the user on 2026-09-18 and stay pending; this partial run does not close BUG-002.
