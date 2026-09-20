# v1.4.47-R3 corrective navigation FIX3

The corrective navigation behavior was already correct after FIX2, but historical
`v1441-auth-ui-consistency-audit.sh` requires the explicit source literal
`!result.authorizationInvalidated` around Review auto-open.

FIX3 preserves the same runtime behavior while restoring that explicit historical guard:

- auth invalidated -> delegated parent is reopened;
- Review auto-open runs only when `!result.authorizationInvalidated`;
- MainActivity remains below the historical 4000-line cleanup ceiling.

No version bump and no historical audit weakening.
