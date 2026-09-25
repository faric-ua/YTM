# v1.4.53 — Evidence Manifest

| Evidence | Source | What it proves | Stored? |
|---|---|---|---|
| v1.4.52 quota screen | user screenshot, 2026-09-25 | Search HTTP 429 can coexist with Search 98/100 and local general-units remainder | Conversation only |
| v1.4.52 Review after quota | user screenshot, 2026-09-25 | 22-track workspace showed 2 ready / 20 quota-related failures | Conversation only |
| v1.4.52 Home after quota | user screenshot, 2026-09-25 | current playlist retained 22 tracks and 20 failures | Conversation only |
| v1.4.52 History after session | user screenshot, 2026-09-25 | History currently had 69 entries; expected interrupted entry not visible | Conversation only |
| v1.4.52 phone recording | user recording, 2026-09-25 | broader quota-session navigation/context | Conversation only |
| v1.4.53 static/full validation | GitHub Actions run 36176662561, source `cf9e3778cc9e1010ba834ed865f6d1ff96c24b33` | release preflight, JVM tests and unsigned release assemble PASS | GitHub Actions |
| v1.4.53 signed candidate | GitHub Actions run 36178783613, app source `454979093c0e108fe629aefe7db4bece97334575` | signed APK build PASS; candidate installed on phone | GitHub Actions + phone |
| Phone QA intermediate — quota split / search accounting | v1.4.53 phone screenshots, 2026-09-26 | Existing WRITE job remained queued after playlist-create HTTP 429. Separate 7-track Search plan showed 51/100, 1 cache hit, 6 new search.list calls; Search completed 7/7 and quota advanced to 57/100, total local units 9606/10000. Confirms Search was still usable and local accounting matched this run; Test 1 quota-stop not reached yet. | phone |
