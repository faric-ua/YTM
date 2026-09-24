# v1.4.51 — Bug Register

## UX-024 — URL Snapshot action footer wastes vertical space in landscape

- Severity: low / visual usability
- Found on: v1.4.51 signed run `35921749405`
- Source: `ba826563032da85fd99eb822c07342c56b2b60f6`
- Area: URL Snapshot resolved-preview fixed footer
- Reproduction: open a completed URL snapshot preview and rotate to landscape.
- Observed: `Зберегти як поточний список` and `Скасувати preview` remain vertically
  stacked even though there is enough width for two readable buttons in one row.
- Expected: peer footer actions use the shared width-aware adaptive action row;
  portrait/narrow layouts may remain stacked.
- Functional impact: none; U51-4 lifecycle behavior passed.
- Corrective: wire the resolved footer to `UiChrome.addAdaptiveActionButtons`
  and make the rule project-wide.
- Corrective app source: `721c712ef9f59f96acb9782036f21506a3dc3464`.
- Signed retest: run `35938232310`, source `05d01e46af2b99acb5a483ad13c3f5a87f849271`.
- Retest result: **PASS / CLOSED** — real-phone landscape screenshot shows both resolved-preview footer actions in one horizontal adaptive row with readable labels.

When a finding appears, record:
- ID;
- severity;
- exact source/build identity;
- reproduction;
- expected behavior;
- observed behavior;
- evidence;
- corrective source/run;
- real-phone retest result.

Do not rewrite v1.4.50 historical bug evidence.

## UX-025 — URL field does not visibly wrap long URLs

- Severity: low / usability.
- Found on: v1.4.51 signed run `35938232310`.
- Source: `05d01e46af2b99acb5a483ad13c3f5a87f849271`.
- Area: URL Snapshot URL input.
- Observed: long URLs scroll/clip horizontally and only one line is effectively visible.
- Expected: URL input wraps into a readable 2–3 line text area.
- Corrective source: `e7404eb3c4760c2fcb0c86d3ec6b3f9d9f8faffd`.
- Signed retest: run `35941777241`, source `2bd57b3996787517f0c1ce8b45e40d74671ebee3`.
- Retest result: **PASS / CLOSED** — long URL wraps visibly across multiple lines.

## BUG-035 — Home quick Export opens Import

- Severity: medium / navigation semantics.
- Found on: v1.4.51 signed run `35938232310`.
- Source: `05d01e46af2b99acb5a483ad13c3f5a87f849271`.
- Area: Home → `Швидкі дії файл/плейлист` → `Експорт`.
- Observed: Export was wired to the same `openImportScreen()` callback as Import.
- Expected: Export opens the existing current-playlist `YTM Project / export` actions.
- Corrective source: `e7404eb3c4760c2fcb0c86d3ec6b3f9d9f8faffd`.
- Implementation: reuse `ReviewActivity.EXTRA_OPEN_PROJECT_ACTIONS`.
- Signed retest: run `35941777241`, source `2bd57b3996787517f0c1ce8b45e40d74671ebee3`.
- Retest result: **PASS / CLOSED** — Home quick Export opens the existing current `YTM Project / export` actions.

## UX-026 — URL field needs one-tap clear action

- Severity: low / usability.
- Requested after U51-5 corrective phone PASS.
- Area: URL Snapshot URL input.
- Expected: a small clear control is available inside the field at the right edge, vertically centered, without covering wrapped URL text.
- Corrective: 44dp inline clear button, extra right text padding, clear-only behavior.
- Retest: pending signed phone QA.
