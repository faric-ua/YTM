#!/data/data/com.termux/files/usr/bin/bash

YTM_REPO_DIR="${YTM_REPO_DIR:-/storage/emulated/0/Documents/YTM}"
YTM_GH_REPO="${YTM_GH_REPO:-faric-ua/YTM}"
YTM_WORKFLOW="${YTM_WORKFLOW:-build-apk.yml}"
YTM_STATE_DIR="${YTM_STATE_DIR:-$HOME/.ytm-importer}"

ytm_fail() {
  echo "FAIL: $*" >&2
  exit 1
}

ytm_require_repo() {
  [ -d "$YTM_REPO_DIR/.git" ] ||
    ytm_fail "YTM repository not found: $YTM_REPO_DIR"

  ytm_ensure_https_origin
}

ytm_ensure_https_origin() {
  local current
  current="$(git -C "$YTM_REPO_DIR" remote get-url origin 2>/dev/null || true)"

  case "$current" in
    git@github.com:faric-ua/YTM.git|ssh://git@github.com/faric-ua/YTM.git)
      git -C "$YTM_REPO_DIR" remote set-url origin "https://github.com/faric-ua/YTM.git"
      echo "Repaired origin transport: SSH → HTTPS"
      ;;
  esac
}

ytm_branch() {
  local branch
  branch="$(git -C "$YTM_REPO_DIR" branch --show-current)"
  [ -n "$branch" ] ||
    ytm_fail "Cannot resolve current YTM branch"
  printf '%s\n' "$branch"
}

ytm_remote_head() {
  local branch="$1"
  local sha
  sha="$(
    git -C "$YTM_REPO_DIR" ls-remote --heads origin "$branch" |
      awk 'NR == 1 {print $1}'
  )"
  [ -n "$sha" ] ||
    ytm_fail "Remote branch not found: origin/$branch"
  printf '%s\n' "$sha"
}

ytm_require_gh() {
  command -v gh >/dev/null 2>&1 ||
    ytm_fail "GitHub CLI (gh) is not installed"
  gh auth status >/dev/null 2>&1 ||
    ytm_fail "GitHub CLI is not authenticated"
}

ytm_require_clean() {
  [ -z "$(git -C "$YTM_REPO_DIR" status --porcelain=v1 --untracked-files=all)" ] ||
    ytm_fail "Working tree is not clean; inspect Status before sync"
}
