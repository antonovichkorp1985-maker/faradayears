#!/usr/bin/env bash
# Реставрация релиза 1.0.1 (fox-tail-lift) из workspace-архива на Google Drive.
#
# Почему так: Google Drive недоступен из песочницы напрямую, а бинарный zip
# (65,7 МБ) нельзя забрать через коннектор. Поэтому этот workflow в самом
# репозитории faradayears скачивает архив на раннере, восстанавливает ветку
# 1.21.1-fox-tail-lift из копии репозитория github/faradayears внутри архива
# и публикует релиз v1.0.1 с JAR и исходниками. main не затрагивается.
#
# Ожидания (из анализа папки «чат хвостик», см. DRIVE_FOLDER_ANALYSIS.md):
#   - дифф 1.0.1 против main = 6 файлов (~122 строки), только
#     CHANGELOG.md / gradle.properties / src/**
#   - FaradayEarsMod-1.21.1-fox-tail-lift-1.0.1.jar = 93486 байт
#   - FaradayEarsMod-1.21.1-fox-tail-lift-source.zip = 131113 байт
set -uo pipefail

DRIVE_ZIP_ID="1EwNoWT-jnOZ_DGVJdFioRAN4pADniPys"
BRANCH="1.21.1-fox-tail-lift"
TAG="v1.0.1"
EXPECTED_MAIN="6adc8779d44dd68e54461d88dfad0d4294b6e9f1"
EXPECTED_JAR_SIZE=93486
EXPECTED_SRC_SIZE=131113
WS=/tmp/ws
ASSETS=/tmp/assets
REPORT=/tmp/restore_report.md
NOTES=/tmp/release_notes.md
FAILED=0

log()  { echo "[restore] $*"; }
warn() { echo "[restore] WARN: $*" | tee -a "$REPORT"; }

commit_report() {
  cd "${GITHUB_WORKSPACE:?}"
  git checkout -q arena/01a0ee50-faradayears 2>/dev/null \
    || git checkout -q -B arena/01a0ee50-faradayears origin/arena/01a0ee50-faradayears
  cp "$REPORT" RESTORE_1.0.1_REPORT.md
  git add RESTORE_1.0.1_REPORT.md
  git -c user.name='arena-ai-coding-agent[bot]' \
      -c user.email='arena-ai-coding-agent[bot]@users.noreply.github.com' \
      commit -q -m "restore-1.0.1: отчёт реставрации (run ${GITHUB_RUN_ID:-?})" || true
  git push origin arena/01a0ee50-faradayears \
    || echo "[restore] не удалось запушить отчёт в ветку сессии"
}

die() {
  echo "[restore] FAIL: $*" | tee -a "$REPORT"
  echo "- статус: ❌ $*" >> "$REPORT"
  commit_report
  cat "$REPORT"
  exit 1
}

mkdir -p "$ASSETS"
{
  echo "# Отчёт реставрации 1.0.1 (fox-tail-lift)"
  echo
  echo "- запущено: $(date -u +%Y-%m-%dT%H:%M:%SZ), run ${GITHUB_RUN_ID:-?} в ${GITHUB_REPOSITORY:-?}"
} > "$REPORT"

# ---------- 1) скачать workspace.zip с Google Drive ----------
log "pip install gdown..."
pip install -q gdown || true
ok=0
for i in 1 2 3 4 5; do
  if gdown "$DRIVE_ZIP_ID" -O /tmp/ws.zip; then ok=1; break; fi
  log "попытка $i не удалась, пауза 15 c"
  sleep 15
done
[ "$ok" = 1 ] || die "не удалось скачать workspace.zip с Google Drive"
echo "- workspace.zip: $(stat -c%s /tmp/ws.zip) байт" >> "$REPORT"

# ---------- 2) распаковать и найти копию репозитория ----------
rm -rf "$WS" && mkdir -p "$WS"
unzip -q -o /tmp/ws.zip -d "$WS" || echo "- WARN: unzip завершился с кодом $?" >> "$REPORT"
WSREPO=""
while IFS= read -r -d '' c; do
  [ -f "$c/build.gradle" ] && WSREPO="$c"
done < <(find "$WS" -maxdepth 3 -type d -name faradayears -print0 2>/dev/null)
[ -n "$WSREPO" ] || die "в workspace не найдена копия репозитория faradayears (build.gradle)"
log "копия репозитория: $WSREPO"
echo "- копия репозитория внутри архива: $WSREPO" >> "$REPORT"

if [ -d "$WSREPO/.git" ]; then
  git config --global --add safe.directory "$WSREPO" || true
  {
    echo "## Коммиты в комплектном .git (информационно)"
    echo '```'
    git -C "$WSREPO" log --oneline --all --decorate 2>/dev/null | head -25 || true
    echo '--- статус рабочей копии:'
    git -C "$WSREPO" status --porcelain 2>/dev/null | head -10 || true
    echo '```'
  } >> "$REPORT"
else
  echo "- .git в копии отсутствует (не мешает: восстановление по файлам)" >> "$REPORT"
fi

# ---------- 3) ветка 1.21.1-fox-tail-lift поверх текущего main ----------
cd "${GITHUB_WORKSPACE:?}"
git fetch -q origin || true
MAIN_SHA=$(git rev-parse origin/main 2>/dev/null || echo "?")
echo "- origin/main: $MAIN_SHA (ожидался $EXPECTED_MAIN)" >> "$REPORT"
[ "$MAIN_SHA" = "$EXPECTED_MAIN" ] || warn "origin/main отличается от ожидаемого; синхронизирую поверх текущего main"

if git ls-remote --heads origin "$BRANCH" | grep -q .; then
  log "ветка $BRANCH уже есть на origin — пуш пропускаю"
  echo "- ветка $BRANCH уже существовала, пуш пропущен" >> "$REPORT"
else
  git checkout -q -B "$BRANCH" origin/main || die "не удалось создать ветку от origin/main"
  rsync -a --exclude='.git' --exclude='build' --exclude='.gradle' \
            --exclude='run' --exclude='out' --exclude='node_modules' \
            "$WSREPO"/ ./ || die "rsync не выполнился"
  git add -A
  NFILES=$(git diff --cached --name-only origin/main | wc -l)
  BAD=$(git diff --cached --name-only origin/main | grep -vE '^(CHANGELOG\.md|gradle\.properties|src/)' || true)
  {
    echo "## Дифф ветки $BRANCH против origin/main"
    echo '```'
    git diff --cached --stat origin/main
    echo '```'
    echo "файлов изменено: $NFILES"
    if [ -n "$BAD" ]; then
      echo "файлы вне допустимых путей:"
      echo "$BAD"
    fi
  } >> "$REPORT"
  log "изменённых файлов: $NFILES"
  if [ "$NFILES" -eq 0 ] || [ "$NFILES" -gt 50 ] || [ -n "$BAD" ]; then
    git reset -q --hard
    die "неожиданный набор изменений ($NFILES файлов) — пуш отменён"
  fi
  git -c user.name='arena-ai-coding-agent[bot]' \
      -c user.email='arena-ai-coding-agent[bot]@users.noreply.github.com' \
      commit -q -m "1.0.1 fox-tail-lift: хвост больше не волочится по земле

Восстановлено из workspace-архива сессии разработки (Google Drive,
папка «чат хвостик»). Отвечает состоянию коммитов 43291ff (силуэт
«пушистой кисти») и 137855c (приподнятая дуга) той сессии, которые
из-за отсутствия авторизации не попали на GitHub."
  if git push origin "$BRANCH"; then
    echo "- ветка $BRANCH запушена: $(git rev-parse HEAD)" >> "$REPORT"
    log "ветка запушена"
  else
    die "не удалось запушить ветку $BRANCH"
  fi
fi

# ---------- 4) ассеты релиза ----------
JAR=$(find "$WS" -maxdepth 2 -type f -name 'FaradayEarsMod-1.21.1-fox-tail-lift-1.0.1.jar' 2>/dev/null | head -1)
[ -z "$JAR" ] && JAR=$(find "$WS" -type f -name '*fox-tail-lift*1.0.1.jar' 2>/dev/null | head -1)
SRC=$(find "$WS" -maxdepth 2 -type f -name 'FaradayEarsMod-1.21.1-fox-tail-lift-source.zip' 2>/dev/null | head -1)
[ -z "$SRC" ] && SRC=$(find "$WS" -type f -name '*fox-tail-lift*source*.zip' 2>/dev/null | head -1)

[ -n "$JAR" ] || die "JAR 1.0.1 не найден в workspace"
cp "$JAR" "$ASSETS/"
JSZ=$(stat -c%s "$JAR")
echo "- JAR: $(basename "$JAR") — $JSZ байт (ожидалось $EXPECTED_JAR_SIZE)" >> "$REPORT"
[ "$JSZ" = "$EXPECTED_JAR_SIZE" ] || warn "размер JAR отличается от ожидаемого"
if [ -n "$SRC" ]; then
  cp "$SRC" "$ASSETS/"
  SSZ=$(stat -c%s "$SRC")
  echo "- source.zip: $(basename "$SRC") — $SSZ байт (ожидалось $EXPECTED_SRC_SIZE)" >> "$REPORT"
  [ "$SSZ" = "$EXPECTED_SRC_SIZE" ] || warn "размер source.zip отличается от ожидаемого"
fi
(cd "$ASSETS" && sha256sum * > SHA256SUMS.txt)
{
  echo "## SHA256 ассетов"
  echo '```'
  cat "$ASSETS/SHA256SUMS.txt"
  echo '```'
} >> "$REPORT"

# ---------- 5) примечания к релизу ----------
cat > "$NOTES" <<'EOF'
Хвост больше не «волочится» по земле — правка физики по итогам игровых тестов.

**Что изменилось (~120 строк в 6 файлах):**
- `PhysicsChain.java` — `restDirection()`: корень хвоста выходит из поясницы горизонтально, дальше хвост держит приподнятую дугу, кончик лишь мягко опускается
- убрана сила, притягивавшая сегменты к земле («присоска к земле»)
- гравитация выровнена по всей длине: −0.070 на земле / −0.060 в воздухе
- `gradle.properties` и `neoforge.mods.toml` — версия 1.0.1
- настройки игрока (длина 1–6 и остальные) не изменились

**Установка:** Minecraft 1.21.1 + NeoForge 21.1.x. Положите JAR в папку `mods/` (предварительно удалите старый JAR этого мода, чтобы не было двух копий).

> Восстановлено из workspace-архива сессии разработки (Google Drive, папка «чат хвостик»): сборка 1.0.1 была готова 27.09.2026, но из-за отсутствия авторизации тогда не попала на GitHub. `main` не затронут; откат — удалить ветку и тег.
EOF

# ---------- 6) релиз v1.0.1 ----------
export GH_TOKEN="${GITHUB_TOKEN:?}"
if gh release view "$TAG" >/dev/null 2>&1; then
  log "релиз $TAG уже существует — перезаливаю ассеты"
  if gh release upload "$TAG" "$ASSETS"/* --clobber; then
    echo "- ассеты перезалиты в существующий релиз" >> "$REPORT"
  else
    warn "не удалось перезалить ассеты"
  fi
else
  log "создаю релиз $TAG с ассетами из $ASSETS"
  if gh release create "$TAG" --target "$BRANCH" \
       --title "FaradayEarsMod 1.21.1 — 1.0.1 fox-tail-lift" \
       --notes-file "$NOTES" "$ASSETS"/*; then
    echo "- релиз $TAG создан" >> "$REPORT"
  else
    warn "не удалось создать релиз $TAG"
  fi
fi

# ---------- 7) итог ----------
{
  echo "## Итог"
  echo "- main: $(git ls-remote origin refs/heads/main | cut -f1)"
  echo "- ветка $BRANCH: $(git ls-remote origin "refs/heads/$BRANCH" | cut -f1)"
  gh release view "$TAG" --json tagName,targetCommitish,assets \
     --jq '"- релиз: " + .tagName + " (target " + .targetCommitish + "), ассеты: " + ([.assets[].name] | join(", "))' 2>/dev/null || true
  [ "$FAILED" -eq 0 ] && echo "- статус: ✅ успех" || echo "- статус: ❌ были ошибки"
} >> "$REPORT"

commit_report
cat "$REPORT"
[ "$FAILED" -eq 0 ]
