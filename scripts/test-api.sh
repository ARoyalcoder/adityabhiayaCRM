#!/usr/bin/env bash
# Backend quality gate: unit tests, then integration tests, which start a
# PostgreSQL container through Testcontainers (Docker must be running).
set -euo pipefail

. "$(dirname "$0")/lib.sh"

cd "$REPO_ROOT/apps/api"
exec ./mvnw verify "$@"
