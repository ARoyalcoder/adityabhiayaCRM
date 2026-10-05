#!/usr/bin/env bash
# Runs the backend on the host against the local PostgreSQL container.
# Flyway migrates the database during startup.
set -euo pipefail

. "$(dirname "$0")/lib.sh"
load_env

cd "$REPO_ROOT/apps/api"
exec ./mvnw spring-boot:run \
  -Dspring-boot.run.profiles="${SPRING_PROFILES_ACTIVE:-local}" \
  "$@"
