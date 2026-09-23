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
- Retest: pending signed landscape visual QA.

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
