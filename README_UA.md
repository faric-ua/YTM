# YTM Importer v1.2.2

Третій окремий screen у новій navigation architecture:

`Головний → Черга`

PendingActivity має:

- searchable queue list;
- progress/counters;
- job detail;
- remaining tracks preview;
- masked account identifiers;
- Open in YTM;
- Delete local job;
- Continue.

Важливо: `Continue` не дублює write logic.
PendingActivity повертає `jobId` назад у MainActivity,
де використовується старий перевірений `resumePendingJob()`.

Тепер окремими screens уже є:

- History;
- Data / Backup;
- Pending Queue.

Наступний великий крок — Import / Review.
