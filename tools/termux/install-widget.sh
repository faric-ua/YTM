#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

REPO="${YTM_REPO_DIR:-$HOME/YTM}"
MENU="$REPO/tools/termux/ytm-menu.sh"
SHORTCUT_DIR="$HOME/.shortcuts"
SHORTCUT="$SHORTCUT_DIR/YTM Importer"

[ -f "$MENU" ] || {
  echo "FAIL: YTM menu not found: $MENU" >&2
  exit 1
}

mkdir -p "$SHORTCUT_DIR"
chmod 700 "$SHORTCUT_DIR"

cat > "$SHORTCUT" <<EOF
#!/data/data/com.termux/files/usr/bin/bash
exec bash "$MENU"
EOF

chmod 700 "$SHORTCUT"

echo "YTM Importer Termux:Widget shortcut installed."
echo "Target:"
echo "  $MENU"
echo
echo "Refresh the Termux:Widget if the shortcut does not update immediately."
