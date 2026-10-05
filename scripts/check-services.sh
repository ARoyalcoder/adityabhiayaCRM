#!/usr/bin/env bash
# Smoke test of the running local environment: checks each service directly,
# then the path a browser takes (frontend -> /api -> backend -> PostgreSQL and Redis).
#
#   check-services.sh            backend and Vite dev server on the host
#   check-services.sh --stack    everything in containers (npm run stack:up)
set -uo pipefail

. "$(dirname "$0")/lib.sh"
load_env
require docker
require curl

API_URL="http://localhost:${SERVER_PORT:-8080}"
WEB_URL="http://localhost:${WEB_PORT:-5173}"
if [ "${1:-}" = "--stack" ]; then
  WEB_URL="http://localhost:${WEB_CONTAINER_PORT:-8081}"
fi

failures=0
check() {
  local name="$1"; shift
  if output="$("$@" 2>&1)"; then
    printf '  ok    %-34s %s\n' "$name" "$(echo "$output" | head -c 120)"
  else
    printf '  FAIL  %-34s %s\n' "$name" "$(echo "$output" | head -c 200)"
    failures=$((failures + 1))
  fi
}

http_ok() {
  curl -fsS --max-time 5 "$1"
}

echo "Checking services"
check "PostgreSQL accepts connections" docker exec bos-postgres pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}"
check "PostgreSQL answers a query" docker exec bos-postgres psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -tAc "select 'version ' || current_setting('server_version')"
check "Redis answers PING" docker exec -e REDISCLI_AUTH="${REDIS_PASSWORD}" bos-redis redis-cli ping
check "Backend readiness" http_ok "$API_URL/actuator/health/readiness"
check "Backend GET /api/v1/health" http_ok "$API_URL/api/v1/health"
check "Frontend serves the app" sh -c "curl -fsS --max-time 5 '$WEB_URL/' | grep -o '<title>[^<]*</title>'"
check "Frontend -> backend /api/v1/health" http_ok "$WEB_URL/api/v1/health"

echo
if [ "$failures" -gt 0 ]; then
  echo "$failures check(s) failed."
  exit 1
fi
echo "All services are up."
