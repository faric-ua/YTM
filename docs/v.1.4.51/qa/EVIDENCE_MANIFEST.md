# v1.4.51 — Evidence Manifest

| Evidence | Source | What it proves | Stored? |
|---|---|---|---|
| First signed concrete-playlist run | GitHub Actions run `35880942335`, source `16dd7ea8240fc4d6922070fc0f6f3f7ce8e41d67` | 813-row concrete playlist can resolve and locally commit; exposed long-preview/duplicate UX findings | GitHub Actions + conversation-reported phone evidence |
| Title metadata corrective | Run `35909545473`, source `cc454aba5bcf1172ce2be64757272c8d0355d699` | Cache-first title repair works; exposed cachedAt semantic issue | GitHub Actions + phone evidence recorded in `PHONE_TEST.md` |
| cachedAt corrective | Run `35921749405`, source `ba826563032da85fd99eb822c07342c56b2b60f6` | 9-track fixture keeps T0 through one metadata request and zero-request reopen | GitHub Actions + conversation-reported phone evidence |
| UX-024 corrective | Run `35938232310`, source `05d01e46af2b99acb5a483ad13c3f5a87f849271` | Landscape Save/Cancel footer uses adaptive horizontal action row | GitHub Actions + conversation screenshot evidence |
| UX-025 / BUG-035 corrective | Run `35941777241`, source `2bd57b3996787517f0c1ce8b45e40d74671ebee3` | Long URL wraps; Home Export opens existing export path; direct-video-without-list rejected | GitHub Actions + conversation-reported phone evidence |
| Final accepted APK | Run `35943953149`, source `226d2453ef3b4a34cb7db7be0c42ae84c8f624a0` | Exact signed app source used for UX-026 and final U51-6 handoff/isolation acceptance | GitHub Actions |
| U51-6 rotation before commit | User real-phone result `U51-6.1+` | 813-row preview survives recreation without auto-commit or auto-navigation | Conversation screenshot/report; not committed as binary evidence |
| U51-6 local History detail | User real-phone result `U51-6.2+` | Dedupe commit records 320 imported / 493 duplicates with local-import semantics and no YTM write counters | Conversation screenshots; not committed as binary evidence |
| U51-6 Current Playlist | User real-phone result `U51-6.3+` | Current local workspace contains 320 ready rows and expected leading source order | Conversation screenshots; not committed as binary evidence |
| U51-6 isolation | User real-phone result `U51-6.4+` | Returning Home starts no create/add flow, queue or YTM write | Conversation screenshot/report; not committed as binary evidence |
| Final phone test report | `qa/PHONE_TEST_REPORT_2026-09-24.md` | Consolidated targeted phone-QA result and limitations | Stored in repository |
| Final test run | `qa/TEST_RUN_2026-09-24.md` | Exact run/source and executed acceptance matrix | Stored in repository |
| Stabilization checkpoint | `qa/STABILIZATION_CHECKPOINT.md` | Final tested source/run/tag/checkpoint identity | Stored in repository |

Evidence limitation: the U51-6 screenshots were supplied in the development conversation and were not separately committed into the repository. They are recorded as conversation-reported evidence rather than fabricated repository assets.
