# 05 — Помилки, які стали частиною навчання

Найцінніша документація часто з'являється не після успіху, а після збою.

## 1. Неунікальний patch anchor

### Що сталося

Apply script очікував один текстовий fragment, але він зустрічався кілька разів.

### Ризик

Автоматичний replace міг змінити не ту функцію.

### Guard

- `count == 1`;
- інакше STOP;
- не використовувати «перший збіг» без перевірки.

## 2. Неправильне припущення про current file

### Що сталося

Patch/preflight очікував guard, якого насправді не було в поточному файлі.

### Guard

Патч має будуватися від **фактичного current repository state**, а не від пам'яті про попередній package.

## 3. Literal `\n` замість реального newline

### Що сталося

Generated anchor містив два символи `\` + `n` замість переводу рядка.

### Симптом

Anchor не знаходився, хоча візуально здавався правильним.

### Guard

Self-test відхиляє literal `\n` у multiline anchors.

## 4. `__pycache__` після Python helper

### Що сталося

Self-test сам створив untracked bytecode file і зупинив clean-tree check.

### Guard

- `sys.dont_write_bytecode = True`;
- або `python -B`;
- cache artifacts заборонені в leftover check.

## 5. CRLF у CSV

### Що сталося

Стандартний CSV writer створив `\r\n`.

`git diff --check` показав `^M` як whitespace problem.

### Guard

- generated text = LF-only;
- CSV `lineterminator="\n"`;
- `git diff --check` до commit;
- package self-test перевіряє CR characters.

## Головний урок

Не треба робити workflow складнішим «про всяк випадок».

Додавайте guard після реальної категорії помилки — і документуйте, **який failure він запобігає**.

Так workflow перетворюється на накопичений досвід проєкту.
