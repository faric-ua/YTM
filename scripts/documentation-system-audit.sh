#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

echo "Documentation system audit"
echo "=========================="

bash scripts/assistant-context-audit.sh
bash scripts/release-documentation-audit.sh
python -B scripts/generate-audit-catalog.py --check
python -B scripts/generate-file-manifest.py --check
python -B scripts/export-assistant-project-skeleton.py --check

echo
echo "PASS:"
echo "- assistant context is complete"
echo "- current release documentation contract"
echo "- audit catalog current"
echo "- repository file manifest current"
echo "- migration kit is reproducible"
