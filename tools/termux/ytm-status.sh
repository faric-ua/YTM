#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SELF_DIR/ytm-common.sh"

ytm_require_repo
ytm_require_gh

BRANCH="$(ytm_branch)"
LOCAL="$(git -C "$YTM_REPO_DIR" rev-parse HEAD)"
REMOTE="$(ytm_remote_head "$BRANCH")"
DIRTY="$(git -C "$YTM_REPO_DIR" status --porcelain=v1 --untracked-files=all)"

git -C "$YTM_REPO_DIR" fetch --quiet origin "$BRANCH"
FETCHED="$(git -C "$YTM_REPO_DIR" rev-parse FETCH_HEAD)"

[ "$FETCHED" = "$REMOTE" ] ||
  ytm_fail "Код на GitHub змінився під час перевірки. Запусти «2 — Перевірити, що зараз готово» ще раз."

VERSION="$(
  sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' \
    "$YTM_REPO_DIR/app/build.gradle.kts" |
    head -n 1
)"

run_for_exact_head() {
  local workflow="$1"
  local sha="$2"

  gh run list \
    --repo "$YTM_GH_REPO" \
    --workflow "$workflow" \
    --branch "$BRANCH" \
    --limit 50 \
    --json databaseId,headSha,status,conclusion,createdAt \
    --jq ".[] | select(.headSha == \"$sha\") | [.databaseId,.status,.conclusion,.createdAt] | @tsv" \
    2>/dev/null |
    head -n 1 ||
    true
}

row_id() {
  local row="$1"
  [ -n "$row" ] || return 0
  printf '%s\n' "$row" | cut -f1
}

row_status() {
  local row="$1"
  [ -n "$row" ] || return 0
  printf '%s\n' "$row" | cut -f2
}

row_conclusion() {
  local row="$1"
  [ -n "$row" ] || return 0
  printf '%s\n' "$row" | cut -f3
}

describe_run() {
  local row="$1"

  if [ -z "$row" ]; then
    echo "ще немає"
    return
  fi

  local id status conclusion
  id="$(row_id "$row")"
  status="$(row_status "$row")"
  conclusion="$(row_conclusion "$row")"

  case "$status:$conclusion" in
    completed:success)
      echo "PASS ✅ (run $id)"
      ;;
    completed:*)
      echo "FAIL ❌ ($conclusion, run $id)"
      ;;
    queued:*|in_progress:*)
      echo "виконується ⏳ (run $id)"
      ;;
    *)
      echo "$status (run $id)"
      ;;
  esac
}

VALIDATION_ROW="$(run_for_exact_head validate.yml "$REMOTE")"
SIGNED_ROW="$(run_for_exact_head "$YTM_WORKFLOW" "$REMOTE")"

VALIDATION_STATUS="$(row_status "$VALIDATION_ROW")"
VALIDATION_CONCLUSION="$(row_conclusion "$VALIDATION_ROW")"
SIGNED_STATUS="$(row_status "$SIGNED_ROW")"
SIGNED_CONCLUSION="$(row_conclusion "$SIGNED_ROW")"

if [ "$LOCAL" = "$REMOTE" ]; then
  if [ -z "$DIRTY" ]; then
    CODE_STATE="актуальний ✅"
    CODE_KIND="SYNCED"
  else
    CODE_STATE="актуальний commit, але є локальні зміни ⚠"
    CODE_KIND="DIRTY"
  fi
elif git -C "$YTM_REPO_DIR" merge-base --is-ancestor "$LOCAL" "$REMOTE"; then
  CODE_STATE="потрібно оновити ⚠"
  CODE_KIND="BEHIND"
elif git -C "$YTM_REPO_DIR" merge-base --is-ancestor "$REMOTE" "$LOCAL"; then
  CODE_STATE="локальна гілка попереду GitHub ❌"
  CODE_KIND="AHEAD"
else
  CODE_STATE="локальна гілка розійшлася з GitHub ❌"
  CODE_KIND="DIVERGED"
fi

DOWNLOADED_SOURCE=""
DOWNLOADED_RUN=""
DOWNLOADED_PATH=""
[ -s "$YTM_STATE_DIR/latest-apk.source" ] &&
  DOWNLOADED_SOURCE="$(cat "$YTM_STATE_DIR/latest-apk.source")"
[ -s "$YTM_STATE_DIR/latest-apk.run" ] &&
  DOWNLOADED_RUN="$(cat "$YTM_STATE_DIR/latest-apk.run")"
[ -s "$YTM_STATE_DIR/latest-apk.path" ] &&
  DOWNLOADED_PATH="$(cat "$YTM_STATE_DIR/latest-apk.path")"

if [ "$DOWNLOADED_SOURCE" = "$REMOTE" ] &&
   [ -n "$DOWNLOADED_PATH" ] &&
   [ -s "$DOWNLOADED_PATH" ]; then
  DOWNLOADED_STATE="завантажений ✅ (run $DOWNLOADED_RUN)"
  HAS_CURRENT_DOWNLOAD="yes"
elif [ "$SIGNED_STATUS:$SIGNED_CONCLUSION" = "completed:success" ]; then
  DOWNLOADED_STATE="ще не завантажений"
  HAS_CURRENT_DOWNLOAD="no"
else
  DOWNLOADED_STATE="—"
  HAS_CURRENT_DOWNLOAD="no"
fi

if [ "$CODE_KIND" = "DIRTY" ] ||
   [ "$CODE_KIND" = "AHEAD" ] ||
   [ "$CODE_KIND" = "DIVERGED" ]; then
  NEXT_ACTION="6 — Перевірити локальні зміни"
elif [ "$CODE_KIND" = "BEHIND" ]; then
  NEXT_ACTION="1 — Оновити проєкт"
elif [ "$VALIDATION_STATUS:$VALIDATION_CONCLUSION" = "completed:success" ]; then
  if [ "$SIGNED_STATUS:$SIGNED_CONCLUSION" = "completed:success" ]; then
    if [ "$HAS_CURRENT_DOWNLOAD" = "yes" ]; then
      NEXT_ACTION="4 — Відкрити папку з APK"
    else
      NEXT_ACTION="3 — Завантажити готовий APK"
    fi
  elif [ "$SIGNED_STATUS" = "queued" ] ||
       [ "$SIGNED_STATUS" = "in_progress" ]; then
    NEXT_ACTION="Зачекай завершення збірки, потім повтори «2 — Перевірити, що зараз готово»"
  elif [ -n "$SIGNED_ROW" ]; then
    NEXT_ACTION="Збірка APK не пройшла. Надішли результат ChatGPT."
  else
    NEXT_ACTION="5 — Зібрати новий APK"
  fi
elif [ "$VALIDATION_STATUS" = "queued" ] ||
     [ "$VALIDATION_STATUS" = "in_progress" ]; then
  NEXT_ACTION="Зачекай завершення перевірки, потім повтори «2 — Перевірити, що зараз готово»"
elif [ -n "$VALIDATION_ROW" ]; then
  NEXT_ACTION="Перевірка поточного коду не пройшла. Надішли результат ChatGPT."
else
  NEXT_ACTION="Зачекай автоматичну перевірку GitHub і повтори «2 — Перевірити, що зараз готово»"
fi

echo "YTM Importer $VERSION"
echo "========================================"
echo "Код у Termux:                     $CODE_STATE"
echo "Перевірка поточного коду:         $(describe_run "$VALIDATION_ROW")"
echo "Підписаний APK для поточного коду: $(describe_run "$SIGNED_ROW")"
echo "APK на телефоні:                  $DOWNLOADED_STATE"
echo
echo "Що робити далі:"
echo "  $NEXT_ACTION"
echo
echo "----------------------------------------"
echo "Технічні деталі"
echo "Гілка:        $BRANCH"
echo "Local:        ${LOCAL:0:12}"
echo "Remote:       ${REMOTE:0:12}"
if [ -n "$VALIDATION_ROW" ]; then
  echo "Validate run: $(row_id "$VALIDATION_ROW")"
fi
if [ -n "$SIGNED_ROW" ]; then
  echo "Signed run:   $(row_id "$SIGNED_ROW")"
fi
