# v1.4.8 — Before / After

## Before

```text
long card + Gravity.CENTER inside ScrollView

       [top part can move outside viewport]
---------------- SCREEN TOP ----------------
        ...middle of dialog...
        ...actions...
---------------- SCREEN BOTTOM -------------
```

## After

```text
safe status-bar / cutout inset
---------------- SCREEN TOP ----------------
[title]
[content]
[content]    ↕ scroll
[actions]
---------------- SCREEN BOTTOM -------------
safe navigation / gesture inset
```

Short dialogs are still centered.
