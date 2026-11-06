package com.saney.ytmimporter.ui

/**
 * Batch 17 (faric 2026-11-05 05:45: «на імпорті… кнопочку знака питання…
 * справочну сторінку… максимально все порозписував, що де робить, яка кнопка,
 * що таке бекапи»). The words of the Import «?» window, kept apart from the
 * Activity so a JVM test checks that every Import button is explained.
 *
 * Wording rules from faric:
 * - «YTM» in button names is YouTube Music — faric took it for the name of
 *   this app (05:59), so the help says it plainly;
 * - «Read-only» stays as a status word, explained once: «(лише читання)» (06:02);
 * - no jargon such as «videoId» or «локальний робочий список».
 */
/** Batch 21: the words of a «?» window (Import, «Створити / додати»…). */
interface HelpContent {
    val title: String
    val subtitle: String
    val lead: String
    val sections: List<ImportHelpText.Section>
}

object ImportHelpText : HelpContent {
    enum class Icon { CURRENT, YOUTUBE, BACKUP, FILE, TEXT, HELP, QUOTA }

    data class Entry(val icon: Icon, val title: String, val text: String)

    data class Section(val title: String, val entries: List<Entry>)

    const val TITLE = "Імпорт • довідка"
    const val SUBTITLE = "Що робить кожна кнопка і що буде з вашими даними"
    const val LEAD = "Імпорт лише відкриває плейліст на телефоні. " +
        "У YouTube Music звідси нічого не змінюється."
    override val title: String get() = TITLE
    override val subtitle: String get() = SUBTITLE
    override val lead: String get() = LEAD

    override val sections: List<Section> = listOf(
        Section("Спершу — кілька слів", listOf(
            Entry(Icon.CURRENT, "Поточний плейліст",
                "Будь-який імпорт відкриває плейліст на телефоні як «Поточний плейліст». " +
                    "Це список, з яким ви далі працюєте на Головній: крок 3 «Знайти / " +
                    "перевірити» шукає відео для пісень, крок 4 «Створити / додати» " +
                    "записує їх у плейліст YouTube Music — лише коли ви самі це " +
                    "підтвердите. Поточний плейліст один: новий імпорт займає його місце."),
            Entry(Icon.YOUTUBE, "«YTM» у назвах кнопок",
                "YTM — це YouTube Music, музичний сервіс Google. Не плутати з назвою " +
                    "цього застосунку (YTM Importer)."),
            Entry(Icon.HELP, "Read-only (лише читання)",
                "Так позначено дії, які лише читають ваш YouTube Music: нічого не " +
                    "створюють, не видаляють і не переставляють."),
            Entry(Icon.QUOTA, "Квота YouTube",
                "YouTube дає застосунку обмежену кількість запитів на день. Читання " +
                    "плейлістів витрачає її трохи, пошук відео (крок 3) — найбільше. " +
                    "Файли й backup з телефона квоту не витрачають.")
        )),
        Section("Поточний робочий список", listOf(
            Entry(Icon.CURRENT, "Очистити поточний список",
                "Прибирає з телефона лише поточний плейліст — після підтвердження. " +
                    "Збережені копії, архів History, файли YTM Project і плейлісти " +
                    "в YouTube Music не змінюються.")
        )),
        Section("Імпорт із YouTube/YTM", listOf(
            Entry(Icon.YOUTUBE, "Вибрати плейлист з YTM",
                "Читає через інтернет список плейлістів вашого акаунта YouTube Music " +
                    "(потрібне підключення Google — Головна, крок 2). Вибираєте один — " +
                    "його пісні відкриваються як «Поточний плейліст» уже з точними " +
                    "відео, тому шукати їх знову не треба. Read-only (лише читання)."),
            Entry(Icon.YOUTUBE, "Імпорт за URL",
                "Вставте посилання на плейліст YouTube або YouTube Music — застосунок " +
                    "прочитає його пісні так само, але за посиланням (підходить і чужий " +
                    "публічний плейліст). Mix (посилання зі списком «RD…») YouTube " +
                    "складає сам і щоразу інакше, тому застосункам його не віддає — " +
                    "застосунок так і скаже. Що робити: збережіть Mix у YouTube Music " +
                    "як звичайний плейліст і вставте посилання вже на нього. " +
                    "Read-only (лише читання).")
        )),
        Section("Резервні копії (backup) акаунта", listOf(
            Entry(Icon.BACKUP, "Що таке backup",
                "Копія ваших плейлістів YouTube Music у файлах на телефоні: для " +
                    "кожного плейліста — файл YTM Project (назва, пісні, точні відео), " +
                    "і один файл-зміст manifest.json — що саме збережено. З backup " +
                    "плейліст відкривається без інтернету, навіть якщо в YouTube Music " +
                    "його вже змінили чи видалили. Створення backup лише читає акаунт."),
            Entry(Icon.BACKUP, "Вибрати плейлисти для експорту",
                "Позначаєте галочками потрібні плейлісти й вибираєте папку. У ній " +
                    "з'являється нова папка з датою й часом у назві, а в ній — файли " +
                    "вибраних плейлістів і manifest.json."),
            Entry(Icon.BACKUP, "Експортувати всі плейлисти в папку",
                "Те саме, але одразу для всіх плейлістів акаунта. Порожні плейлісти " +
                    "файлом не зберігаються, проте записані в manifest.json."),
            Entry(Icon.BACKUP, "Відкрити backup / manifest.json",
                "Вибираєте папку зі збереженим backup — застосунок читає manifest.json " +
                    "і показує, які плейлісти там є. Вибраний відкривається як " +
                    "«Поточний плейліст». Без інтернету й без витрати квоти."),
            Entry(Icon.BACKUP, "Оновити backup (incremental)",
                "Порівнює плейлісти в YouTube Music зі старим backup і зберігає лише " +
                    "різницю: нові (NEW), змінені (UPDATED), без змін (UNCHANGED), " +
                    "зниклі (MISSING). Старий backup не змінюється — поруч з'являється " +
                    "нова папка. Зберігайте нові backup у ту саму спільну папку, " +
                    "що й старі."),
            Entry(Icon.BACKUP, "Зібрати повний backup з ланцюжка",
                "Після кількох оновлень у вас «ланцюжок»: повний backup і кілька " +
                    "різниць. Ця кнопка складає з них один повний backup на останній " +
                    "стан. Працює лише з файлами на телефоні: інтернет і квота не " +
                    "потрібні, старі папки не змінюються.")
        )),
        Section("Імпорт із файлу", listOf(
            Entry(Icon.FILE, "Вибрати файл",
                "Відкриває з телефона файл CSV, TXT або YTM Project. CSV і TXT — " +
                    "список пісень «Виконавець - Пісня»; YTM Project — файл, " +
                    "збережений цим застосунком, уже з точними відео. Android може " +
                    "показати й файли інших типів — це нормально: деякі програми " +
                    "неправильно позначають CSV.")
        )),
        Section("Вставити текст", listOf(
            Entry(Icon.TEXT, "Назва плейлиста (необов'язково)",
                "Як назвати новий плейліст. Можна не заповнювати."),
            Entry(Icon.TEXT, "Імпортувати текст",
                "Один рядок — одна пісня: «Виконавець - Пісня». Підходять дефіси " +
                    "-, – і —, а також нумерація «1.» або «2)». Пісні відкриваються як " +
                    "«Поточний плейліст»; відео для них знайде крок 3 на Головній.")
        )),
        Section("Що буде далі", listOf(
            Entry(Icon.HELP, "Після імпорту",
                "Ви повертаєтесь на Головну. Крок 3 «Знайти / перевірити» шукає відео " +
                    "для пісень, у яких їх ще нема, і відкриває перевірку. Плейліст у " +
                    "YouTube Music з'являється лише на кроці 4, коли ви самі " +
                    "підтвердите.")
        ))
    )

    /** Titles of all entries — every Import button must be one of them. */
    val titles: List<String> get() = sections.flatMap { s -> s.entries.map { it.title } }
}
