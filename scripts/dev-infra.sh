#!/usr/bin/env bash
# Local infrastructure (PostgreSQL and Redis), or the whole stack in containers.
#
#   dev-infra.sh up        PostgreSQL + Redis, waits until both are healthy
#   dev-infra.sh stack     PostgreSQL + Redis + backend + frontend containers
#   dev-infra.sh status    container health
#   dev-infra.sh logs      follow logs of every running service
#   dev-infra.sh down      stop everything (data stays in the pgdata volume)
#   dev-infra.sh reset     stop everything and delete the local database volume
set -euo pipefail

. "$(dirname "$0")/lib.sh"
load_env
require docker

COMPOSE=(docker compose --env-file "$REPO_ROOT/.env" -f "$REPO_ROOT/docker/compose.yaml")

wait_healthy() {
  local deadline=$((SECONDS + ${2:-60}))
  echo "Waiting for $1 to become healthy..."
  while [ $SECONDS -lt $deadline ]; do
    local state
    state="$(docker inspect -f '{{.State.Health.Status}}' "$1" 2>/dev/null || echo missing)"
    if [ "$state" = "healthy" ]; then
      echo "$1 is healthy."
      return 0
    fi
    sleep 2
  done
  echo "$1 did not become healthy in time." >&2
  docker logs --tail 30 "$1" >&2 || true
  return 1
}

case "${1:-up}" in
  up)
    "${COMPOSE[@]}" up -d postgres redis
    wait_healthy bos-postgres
    wait_healthy bos-redis
    ;;
  stack)
    "${COMPOSE[@]}" --profile app up -d --build
    wait_healthy bos-postgres
    wait_healthy bos-redis
    wait_healthy bos-api 180
    echo "Frontend: http://localhost:${WEB_CONTAINER_PORT:-8081}"
    ;;
  status)
    "${COMPOSE[@]}" --profile app ps
    ;;
  logs)
    "${COMPOSE[@]}" --profile app logs -f
    ;;
  down)
    "${COMPOSE[@]}" --profile app down
    ;;
  reset)
    "${COMPOSE[@]}" --profile app down -v
    ;;
  *)
    echo "Usage: $0 {up|stack|status|logs|down|reset}" >&2
    exit 2
    ;;
esac
