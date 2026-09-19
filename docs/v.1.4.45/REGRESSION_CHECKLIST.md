# v1.4.45 Regression Checklist

Static/build checks do not equal phone PASS.

- [x] shared `UiChrome.emphasizedTitle(...)` exists
- [x] dialog headers use the shared emphasized title
- [x] major full-screen top bars use the shared emphasized title
- [x] Blue / Green / Neon use their own theme accent
- [x] title emphasis does not recolor ordinary body text
- [x] title emphasis does not change Home workflow-state semantics
- [x] portrait title fit
- [x] landscape title fit
- [x] representative dialog title fit
- [x] Back / action buttons remain usable
