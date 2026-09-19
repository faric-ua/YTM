# v1.4.45 Regression Checklist

Static/build checks do not equal phone PASS.

- [ ] shared `UiChrome.emphasizedTitle(...)` exists
- [ ] dialog headers use the shared emphasized title
- [ ] major full-screen top bars use the shared emphasized title
- [ ] Blue / Green / Neon use their own theme accent
- [ ] title emphasis does not recolor ordinary body text
- [ ] title emphasis does not change Home workflow-state semantics
- [ ] portrait title fit
- [ ] landscape title fit
- [ ] representative dialog title fit
- [ ] Back / action buttons remain usable
