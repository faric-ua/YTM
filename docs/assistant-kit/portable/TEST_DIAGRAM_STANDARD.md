
# Portable Test Diagram Standard

## Route

Every route diagram starts from the real user entry point.

Show:

- parent screen;
- action/button;
- child screen;
- modal/system UI;
- relevant states;
- Back/Cancel ownership;
- rotation/recreation point when relevant;
- expected PASS/FAIL condition.

## System-state branches

For lifecycle-sensitive tests explicitly represent:

- before rotation;
- Activity recreation;
- restored semantic state;
- guarantee that no operation auto-runs.

## Navigation

If one child can be reached from different parents, create separate routes when
Back ownership differs.

## Phone QA

A diagram is a test definition, not proof of execution.
Executed PASS/FAIL belongs in the test-run report.
