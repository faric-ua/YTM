# YTM Importer v1.4.13

Cleanup Wave 3 — SearchCoordinator.

Test status:
- v1.4.12: **NOT TESTED**
- v1.4.13: **NOT TESTED YET**

Search planning/execution/cache/quota/candidate-state logic has been extracted
from MainActivity into a dedicated non-UI `SearchCoordinator`.

MainActivity: 3689 → 3620 lines.

Q-002 dialog entrance motion remains deferred by the user.
