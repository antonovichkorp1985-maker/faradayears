# Пошаговая инструкция: загрузка мода на GitHub, релиз и публикация

Время выполнения: **~20–30 минут**. Всё делается один раз; дальше обновления — по 2 минуты.

---

## Шаг 0. Что нужно установить (только один раз)

1. **Git for Windows** — https://git-scm.com/download/win
   (мастер установки: оставляйте всё по умолчанию, просто Next → Install → Finish)
   *Альтернатива без командной строки:* **GitHub Desktop** — https://desktop.github.com/
2. **Аккаунт GitHub** — https://github.com/signup

После установки Git откройте **PowerShell** и проверьте:
```powershell
git --version
```
Должно напечатать что-то вроде `git version 2.4x.x`.

## Шаг 1. Создать репозиторий на GitHub

1. Зайдите на https://github.com/new
2. **Repository name:** `faradayears` (именно так — этот адрес уже прописан в `mods.toml`)
3. **Visibility:** `Public` (для бесплатного GitHub Actions и публичной страницы)
4. **НЕ** включайте «Add a README file», «Add .gitignore», «Add a license» — всё уже есть в проекте, иначе будет конфликт
5. Нажмите **Create repository**
6. Останетесь на странице с инструкцией — она понадобится на шаге 3

## Шаг 2. Подготовить папку проекта

1. Распакуйте архив `KineticEarsTails-Final.zip` в папку, например: `C:\mods\faradayears`
2. Внутри должны быть: `build.gradle`, `gradlew.bat`, `src`, `README.md`, `LICENSE`, `.github\` и т.д.
3. Откройте **PowerShell** в этой папке (в Проводнике: адресная строка → введите `powershell` → Enter)

## Шаг 3. Первый push (команды копировать по одной)

```powershell
git init
git add .
git commit -m "KineticEarsTails 1.0.0 (V56): ears, tail physics, body, pouch, GUI"
git branch -M main
git remote add origin https://github.com/ВАШ_ЛОГИН/faradayears.git
git push -u origin main
```

⚠️ При первом push Git попросит авторизацию. **Важно:** пароль от аккаунта НЕ подходит — нужен **Personal Access Token**:
1. GitHub → иконка профиля → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)** → **Generate new token**
2. Галочка `repo` (полный доступ к репозиториям) → **Generate token**
3. Скопируйте токен (показывается один раз!) и вставьте его вместо пароля.
   *Проще:* вместо токена можно использовать **GitHub CLI** (`winget install GitHub.cli`, затем `gh auth login`) — он всё сделает сам.
   *Самый простой вариант:* GitHub Desktop — там логин через браузер, без токенов.

## Шаг 4. Проверить автоматическую сборку (GitHub Actions)

1. На GitHub откройте репозиторий → вкладка **Actions**
2. Должен крутиться workflow **«Build KineticEarsTails»** (зелёная галочка = успех, красный крест = смотрите логи, пришлите мне текст ошибки)
3. Успешная сборка в CI означает, что **мод собирается на серверах GitHub без вашего участия** — проблема с TLS на вашем ПК больше не помеха!

## Шаг 5. Создать релиз (одна команда)

```powershell
git tag v1.0.0
git push origin v1.0.0
```

Workflow автоматически:
- соберёт `KineticEarsTails-1.21.1-1.0.0.jar`
- создаст **GitHub Release** с прикреплённым jar-файлом → вкладка **Releases** на странице репозитория
- этот релиз и есть «страница загрузки» мода — ссылку можно давать друзьям

Дальнейшие обновления: правите код → `git add .` → `git commit -m "описание"` → `git push` → новый тег `v1.0.1` → новый релиз. Всё.

## Шаг 6. CurseForge (главная площадка модов Minecraft)

1. Регистрация: https://www.curseforge.com/register
2. Войти → **My Projects** → **Create Project**
3. **Project Name:** `Kinetic Ears & Tails`
4. Вставьте описание из файла **`CURSEFORGE_DESCRIPTION.md`** (английский вариант — основной)
5. **Game:** Minecraft → **Game version:** 1.21.1 → **Loader:** Forge
6. Загрузите jar: `build/libs/KineticEarsTails-1.21.1-1.0.0.jar` (или скачайте из GitHub Release)
7. Загрузите **2–3 скриншота** (обязательно):
   - пресет «Огненные кисточки» крупным планом (ушки + хвост)
   - хвост с хитбоксами (`F3+B`) на фоне
   - поясной мешочек + GUI с орбитальной камерой
8. **Source code / Issues:** укажите ссылку на GitHub-репозиторий (CurseForge любит открытые исходники)
9. **License:** MIT
10. Опубликовать → модерация занимает обычно 1–3 дня, следите за уведомлениями (могут попросить что-то поправить в описании)

## Шаг 7. Modrinth (по желанию, бонус)

1. https://modrinth.com → **Register** → **Dashboard** → **Create project**
2. Те же данные, что и для CurseForge + загрузка jar
3. Модерация на Modrinth быстрее и проще

---

## Чек-лист перед публикацией
- [ ] `README.md` и `README_EN.md` на месте
- [ ] `LICENSE` (MIT) в корне — совпадает с `mods.toml`
- [ ] `CHANGELOG.md`, `CONTRIBUTING.md`, `SECURITY.md` на месте
- [ ] В `mods.toml`: `displayName`, `authors`, `displayURL`, `issueTrackerURL` заполнены
- [ ] Сборка прошла в GitHub Actions (зелёная галочка)
- [ ] Скриншоты готовы (минимум 2)
- [ ] GitHub Release с jar создан

**Если что-то пойдёт не так** — пришлите сюда текст ошибки из логов Actions или вывод PowerShell, разберём вместе.
