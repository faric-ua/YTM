# v1.4.19 — UI Observations

## Import → YouTube/YTM card

Observed on the phone:

1. `Експортувати всі плейлисти в папку` needs two lines, but the fixed-height action button clips the lower part of the text.
2. The bulk-export button sits almost directly against `Вибрати плейлист з YTM`.

Functional impact: **none observed**. The action remains clickable and bulk export succeeds.

Planned v1.4.20 adjustment:

- action buttons use `WRAP_CONTENT` with a minimum comfortable height instead of a hard 54dp height;
- long labels keep up to two lines;
- the second account action gets a 10dp top margin;
- autosizing remains within a readable range.
