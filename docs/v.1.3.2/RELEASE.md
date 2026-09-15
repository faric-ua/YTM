# YTM Importer v1.3.2 — Review wording / Project save feedback

## Виправлення Review

Ручний вибір більше не міняє місцями original track і вибраний YouTube result.

Було:

`So Much In Love ...`
`Заміна для: The Weeknd — Blinding Lights`

Стало:

`The Weeknd — Blinding Lights`
`Ручний вибір: So Much In Love ... • D.O.D - Topic`

Оригінальний track завжди лишається головним рядком.

Для автоматичного match:

`Знайдено: <YouTube title> • <channel>`

Для manual selection:

`Ручний вибір: <YouTube title> • <channel>`

Status для manual MATCHED:

`✓ вибрано`

замість неоднозначного:

`✓ знайдено`.

## Review detail

У detail screen:

- automatic result → `Знайдено:`
- manual result → `Ручний вибір:`

Окремий зайвий рядок `Ручний вибір` після channel прибрано.

## Project save confirmation

Після успішного Save Project toast тепер містить людське ім'я project:

`Project «Вставлений список» збережено`

і нижче фактичне ім'я файлу, яке повернув Android document provider.

Якщо користувач перейменував файл у системному picker,
у повідомленні показується саме збережене ім'я.

## Версія

```text
versionCode = 33
versionName = "1.3.2"
```
