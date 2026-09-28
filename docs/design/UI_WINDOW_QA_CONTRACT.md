# YTM Importer — UI Window QA Contract

## Мета

Це обов'язковий стандарт для кожного нового або зміненого user-facing екрана, модалки,
Help window, chooser, overlay, progress/result state та іншого інтерактивного UI.

Мета — перевіряти не лише те, що вікно "відкрилося", а весь його контракт:
**елементи → розміщення → вигляд → текст → стани → дії → navigation → lifecycle**.

Якщо змінюється спільний UI-компонент, аудит застосовується до всіх основних вікон,
які його використовують.

---

## 1. Window spec перед phone QA

Для кожного нового/зміненого вікна треба явно зафіксувати:

- назву вікна;
- батьківський екран і точну дію входу;
- основні елементи;
- порядок елементів;
- що scrollable, а що fixed;
- primary / secondary / destructive actions;
- стани enabled / disabled / loading / error / completed / blocked;
- Back / Cancel / Close semantics;
- rotation/recreation contract;
- restart/force-close contract, якщо вікно керує durable operation;
- очікуваний екран після кожної завершальної дії.

Не можна вважати вікно протестованим лише тому, що його content видно.

---

## 2. Обов'язковий набір елементів і перевірок

Для кожного вікна перевіряємо, де застосовно:

### Header / top bar

- title читається повністю або має контрольований max-lines;
- Back знаходиться у звичному місці і повертає до правильного parent;
- Back не запускає, не повторює і не скасовує remote work неявно;
- system status bar / cutout не перекривають content.

### Main content

- title/entity name має найвищу інформаційну вагу;
- status не змішується з назвою;
- metadata / counters / technical details візуально другорядні;
- cards/tiles мають однакові margins, radius і hierarchy;
- довгі назви не виштовхують action controls;
- empty/error/loading/completed states не показують stale content з попереднього стану.

### Scroll + footer

Це **обов'язкова структура**, а не рекомендація:

- user-facing action buttons у modal / Help / chooser / result / confirmation window
  розміщуються в **fixed bottom footer**;
- footer з action-кнопками **не входить у scrollable content**;
- action-кнопки мають залишатися видимими без прокручування незалежно від довжини тексту;
- scrollable є тільки content-area між header/top area і fixed footer;
- довгий текст, cards, lists, diagnostics та пояснення прокручуються всередині content-area;
- footer не перекриває останній content item: scroll container має мати коректний bottom inset/padding;
- primary / secondary / destructive action order не змінюється через scroll;
- footer може адаптивно перебудовувати кнопки row ↔ stack за шириною, але сам footer залишається fixed;
- footer не повинен рухатися разом із content при scroll;
- усі actions мають бути одночасно reachable і видимі в portrait та landscape;
- scroll position не повинен змінювати semantic state.

Виняток допускається лише для **не-action tile/button**, який є частиною самого content
(наприклад картка-навігація у списку). Такий елемент не вважається footer action.

---

## 3. Button standard

### 3.1 Семантика

У кожній action group:

- один чіткий primary action, якщо сценарій має "наступний крок";
- secondary actions не повинні виглядати сильніше за primary;
- destructive action має окремий danger tone і явне підтвердження;
- disabled button має бути **очевидно disabled**, але label повинен залишатися читабельним;
- enabled/disabled state має відповідати реальній можливості виконати дію, а не блокувати
  безпечний recovery path.

### 3.2 Текст на кнопках

User-facing button label:

- короткий;
- описує дію дієсловом;
- не містить raw enum/state names;
- не містить implementation terms без потреби;
- не використовує slash-heavy або debug-like wording;
- не дублює великий explanatory paragraph;
- не має вимагати від користувача знання внутрішніх термінів типу
  `PREPARED`, `remote baseline`, `append-safe wave`, `explicit resume`.

Приклади:
- добре: `Продовжити`, `Створити сесію`, `Скасувати`;
- погано як primary UI: `Progress / pause / explicit resume після restart`.

Технічні деталі дозволені у secondary details / diagnostics / Help, але не як головний action text.

### 3.3 Layout кнопок

Проєкт використовує width-first правило:

- 2–3 peer actions → один рядок лише коли **кожна** кнопка має достатню ширину;
- якщо label починає ламатися/стискатися → весь action group переходить у vertical stack;
- не зменшувати font лише щоб "втиснути" ряд;
- footer/confirmation action labels за замовчуванням мають бути однорядковими;
- дворядкова кнопка допустима лише як навмисний tile-like action, а не як випадковий overflow;
- touch target не менше стандартного phone-friendly розміру;
- button height, padding і visual tone мають бути консистентними у portrait/landscape.

Спільний механізм:
`UiChrome.useHorizontalActionRow(...)` / `UiChrome.addAdaptiveActionButtons(...)`.

---

## 4. Portrait / landscape — окремі QA стани

Кожне нове/змінене вікно перевіряємо мінімум у:

1. portrait;
2. landscape;
3. portrait → landscape;
4. landscape → portrait.

Для кожного стану перевіряємо:

- чи не обрізаний title;
- чи не ламається button text;
- чи не перекритий content;
- чи fixed footer весь час залишається видимим;
- чи action buttons не поїхали в scrollable content;
- чи footer не займає непропорційно багато місця;
- чи cards/tiles не стають надто вузькими;
- чи primary action залишається очевидним;
- чи scroll дозволяє дістатися останнього content-елемента **без необхідності шукати кнопки**;
- чи modal залишається в межах viewport;
- чи rotation не виконує action автоматично.

Orientation-name не визначає layout напряму — рішення базується на фактичній ширині.

---

## 5. State matrix

Якщо вікно підтримує стан, мінімально перевіряємо релевантні з:

- Initial / Ready;
- Loading / Preparing;
- Empty;
- Validation error;
- Recoverable error;
- Blocked;
- Running;
- Paused;
- Completed;
- Partial failure;
- Disabled action;
- Enabled action.

Для кожного state перевіряємо одночасно:

- текст;
- visual tone;
- доступні buttons;
- disabled buttons;
- counters/status;
- що стан не суперечить фактичній операції;
- що після переходу не лишається stale message попереднього state.

---

## 6. Lifecycle matrix

### Rotation / recreation

Для кожного state, де це має сенс:

- rotate;
- Activity recreation;
- той самий semantic state відновлено;
- active modal/help/editor відновлено над тим самим parent;
- draft input не втрачений;
- remote/local operation не стартує повторно.

### Back / Cancel / Close

Окремо тестуємо:

- Back;
- Cancel;
- Close;
- system Back;
- повернення з child Activity;
- повернення після external app/system picker, якщо застосовно.

Кожна дія має мати одного зрозумілого owner і очікуваний destination.

### Force-close / restart

Обов'язково для durable або remote-write flow:

- force-close в контрольованій точці;
- reopen;
- жодного auto-resume;
- durable counters/ledger збережені;
- explicit Resume доступний лише коли recovery безпечний;
- uncertain mutation не виконується blind retry;
- duplicate remote mutation не створюється.

---

## 7. Content stress

Перед PASS перевірити, де застосовно:

- коротку назву;
- дуже довгу назву;
- довгий button label;
- 0 items;
- 1 item;
- багато items;
- long error text;
- keyboard visible;
- technical details expanded;
- narrow width.

Не можна вважати layout стабільним лише на "ідеальному" короткому тексті.

---

## 8. Theme + accessibility sanity

Основний phone QA виконується мінімум на активній темі кандидата.

Для shared UI змін перевірити Neon / Blue / Green:

- primary/secondary/danger tone;
- disabled contrast;
- text contrast;
- borders/accent;
- selected/focused states.

Колір ніколи не є єдиним носієм статусу — status має також мати текст/іконку.

---

## 9. Evidence contract

Для UI/window PASS evidence має доводити конкретні тези.

Мінімум для нового або суттєво зміненого вікна:

- portrait screenshot;
- landscape screenshot;
- screenshot ключового modal/state;
- video для lifecycle/dynamic flow, якщо є rotation/restart/progress/remote work.

У звіті окремо відмічаємо:

- Visual PASS;
- Layout PASS;
- Button semantics PASS;
- Rotation PASS;
- Navigation PASS;
- Restart PASS, якщо застосовно;
- Functional action PASS.

Якщо перевірена лише частина — статус **PARTIAL**, не загальний PASS.

---

## 10. Canonical window composition

Для нового modal / Help / chooser / result / confirmation window базова композиція така:

1. **Header / title area** — fixed у верхній частині, якщо вікно має окремий header.
2. **Scrollable content area** — займає весь доступний простір між header і footer.
3. **Fixed action footer** — завжди останній layout-блок і завжди видимий.
4. Footer actions адаптуються за шириною через row/stack, але **ніколи не переносяться у scroll**.
5. Якщо content не вміщується — зменшується viewport content-area, а не доступність footer.
6. Якщо відкрито keyboard/IME і actions потрібні для завершення форми, footer має лишатися доступним
   відповідно до конкретного screen contract; його не можна втрачати під клавіатурою без явної причини.
7. Для Help window типова кнопка `Зрозуміло` / `Закрити` належить fixed footer.
8. Для chooser primary/secondary actions належать fixed footer.
9. Для result window `Готово` / `Закрити` / retry action належать fixed footer.
10. Scrollbar, якщо є, відноситься лише до content-area і не охоплює footer.

Антипатерни:
- action button в кінці великого ScrollView;
- необхідність прокручувати донизу, щоб знайти `Закрити` / `Зрозуміло` / `Продовжити`;
- footer, який зникає при scroll;
- дублювання однієї і тієї ж завершальної дії і в content, і у footer;
- різна footer-архітектура portrait/landscape без функціональної причини.

## 11. Release rule

Нове або суттєво змінене user-facing вікно не вважається phone-accepted,
поки не пройдено його window audit за цим контрактом.

Release-specific `PHONE_TEST.md` може скорочувати нерелевантні пункти,
але має явно назвати, що саме не застосовується або не перевірялося.

Цей документ є project-wide source of truth разом з:

- `RESPONSIVE_ACTION_LAYOUT_CONTRACT.md`;
- `TILE_UI_CONTRACT.md`;
- `qa/MASTER_TEST_PLAN.md`;
- release-specific `docs/v.X.Y.Z/qa/PHONE_TEST.md`.


## 12. Static-first enforcement

Phone QA is not the place to discover violations that can be proven from source.

Before a new or changed window enters phone QA:

1. the shared window/footer static audit must pass;
2. the release preflight must pass on the exact app source;
3. known shared-component violations are fixed at the shared layer before feature-specific retesting starts;
4. only then is a signed candidate produced for visual/lifecycle evidence.

If a phone finding exposes a **shared** UI defect, stop the feature test at that
checkpoint. Do not keep walking through later feature steps while the common window
contract is known to be broken. Fix the shared component, audit all consumers,
revalidate, install in-place when state must be preserved, and resume from the
recorded phone checkpoint.

Canonical enforcement:
- `scripts/ui-window-contract-audit.sh`;
- `scripts/release-preflight.sh`.

A shared `UiChrome` change is never accepted from one screenshot alone. Its static
audit scope covers every shared modal consumer, and phone QA uses a representative
matrix rather than rediscovering the same defect window by window.

## 13. Canonical shared-modal architecture

Action-bearing YTM modals use one composition:

1. fixed header/title area;
2. one scrollable content viewport;
3. one fixed action footer outside that scroll viewport.

The canonical renderer is the shared fixed-footer pipeline in `UiChrome`.
`showMessageDialog`, `showRecordDialog`, `showContentDialog`,
`showMultiChoiceDialog`, menu close actions, and compatibility
`StableAlertBuilder` routes must converge on it.

A new action-bearing modal must not call the legacy whole-card scroll shell directly.
That shell may exist only for genuinely actionless content.

For Help windows, the UI shell is only half the contract: the owning Activity must
also persist the semantic “Help is open” state and restore it after recreation without
executing an action.
