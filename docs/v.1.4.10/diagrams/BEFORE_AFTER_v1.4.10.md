# v1.4.10 — Before / After

## Step 2

```text
BEFORE

[ 1. Імпорт        ]
          [ 2. Google / YTM ✓ ]   ← shifted down

AFTER

[ 1. Імпорт        ][ 2. Google / YTM ✓ ]
↑ same top                           ↑
↓ same bottom                        ↓
```

## Dialog

```text
BEFORE
centered
   ↓
height remeasure
   ↓
jumps to safe top

AFTER
safe top
   ↓
card is TOP anchored from first visible frame
   ↓
content height only changes scrolling
```
