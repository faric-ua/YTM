# A. Додаток: типи файлів YTM Importer
<a id="secA"></a>
| Тип | Типова назва / структура | Для чого |
| --- | --- | --- |
| Full Local Backup | YTM_Backup_*.json; format=ytm-importer-local-backup | Повне локальне відновлення History/Queue/workspace/caches без rollback quota. |
| History Backup | YTM_History_*.json; масив History entries | Відновити тільки History. |
| Pending Diagnostics | YTM_Pending_*.json; масив Pending jobs | Технічний експорт Queue, не Full Restore. |
| YTM Project | *.ytm-project.json; format=ytm-importer-playlist-project | Переносний проєкт одного плейлиста з exact videoId. |
| Account Library Backup | Папка + manifest.json + YTM Project | Архів/backup remote-бібліотеки акаунта. |
| Diagnostics TXT | текстовий файл | Підтримка/діагностика без OAuth token; ідентифікатори маскуються. |

> **Тип файла визначається вмістом**  
> Актуальний file-library контракт не покладається лише на розширення .json або префікс назви. Для destructive restore остаточну перевірку все одно виконує власник формату перед будь-якою мутацією.

[Назад до індексу](README.md)

# B. Додаток: актуальний стан документації
<a id="secB"></a>
Цей документ згенеровано для YTM Importer v1.4.55 зі стану гілки feat/v1.4.55-ux-hardening. Source snapshot під час формування: 47a14443bafa93dcef7fd930a394c435e6098f36.

Окремий Recovery Center phone acceptance закрито на signed candidate source efab5dc15ce389aa50e5d9d15aa1cdd78e60e08f, run 37338681198. Після цього в гілці продовжилася #54 file-library робота; тому частина нового файлового UX може бути новішою за APK, який був встановлений під час Recovery Center тесту.

## Як розширювати документ надалі
<a id="secB1"></a>
- Додавати нову функцію в тематичний розділ, а не тільки в changelog.
- Якщо з’явився новий екран — додати його до змісту та сценаріїв.
- Якщо змінився небезпечний workflow (Restore/Delete/Write/Rollback) — оновити також callout з наслідками.
- Для нових форматів файлів — оновити Appendix A та розділ 16.
- Після великої зміни оновити source snapshot і дату документа.

> **Живий документ**  
> Канонічну текстову версію доцільно зберігати в репозиторії в docs/user-guide/. DOCX — читабельний артефакт для користувача; Markdown-source простіше редагувати й версіонувати.

[Назад до індексу](README.md)

---

> **Головне правило**  
> Коли сумніваєтесь: спочатку дивіться, що саме є локальним станом, що є read-only preview, а що є явною remote mutation. YTM Importer навмисно розділяє ці кроки, щоб випадковий Back/rotation/restart не повторив дію.
