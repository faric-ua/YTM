# v1.4.47-R3 Flow Result / Lifecycle R6 — FIX1B

The prior FIX1 also used textual source matching against the R6 apply script.
That was incorrect because the packaged R6 apply script serializes `OPS` as one
Python literal list rather than preserving the original `add(...)` source calls.
Therefore FIX1 failed closed with `old=0, new=0`.

FIX1B patches the `OPS` entry semantically:
- parses the R6 apply script with Python `ast`;
- locates exactly one operation named `Release preflight checks R6 audit` for
  `scripts/release-preflight.sh`;
- verifies it still contains the known wrong anchor;
- changes only its `old` / `new` values to the current preflight order;
- reparses and verifies the exact resulting operation;
- is idempotent.

No application/runtime source is changed by FIX1B itself.
