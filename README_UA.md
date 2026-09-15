# YTM Importer v1.4.1

Технічний cleanup після перенесення Step 4 у `DestinationActivity`.

Що змінилось:

- старий Destination/Create flow через AlertDialog видалено з MainActivity;
- новий DestinationActivity залишається єдиним UI-шляхом Step 4;
- create/append, duplicates, OAuth, quota, Pending Queue і History core не переписувалися;
- додано `scripts/mainactivity-audit.sh`.

Відкрите питання Q-001 з v1.3.2 залишається OPEN і буде переглянуте пізніше.

Наступний cleanup: старі Import / History / Data / Pending dialog-шляхи.
