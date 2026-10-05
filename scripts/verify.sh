#!/usr/bin/env bash
# Runs every check this repository has, in the order CI would run them.
set -euo pipefail

. "$(dirname "$0")/lib.sh"

echo "== frontend: lint =="
npm run lint --prefix "$REPO_ROOT"

echo "== frontend: type-check and build =="
npm run build:web --prefix "$REPO_ROOT"

echo "== backend: unit and integration tests =="
"$REPO_ROOT/scripts/test-api.sh"

echo
echo "All checks passed."
