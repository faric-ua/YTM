# v1.4.18 QA status-audit fix

The phone-QA package correctly changed the release status from `NOT TESTED YET`
to `PARTIALLY PHONE-TESTED — PASS FOR G01`.

`qa-plan-audit.sh` was updated, but `release-preflight.sh` still expected the
old status and stopped before commit.

This hotfix updates that guard and also updates the phone-QA apply script so
future re-application remains reproducible.

No app source code or version bump is included.
