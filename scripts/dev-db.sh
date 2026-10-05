#!/usr/bin/env bash
# Starts, stops or tails the local PostgreSQL container.
set -euo pipefail

. "$(dirname "$0")/lib.sh"
load_env
require docker

COMPOSE=(docker compose --env-file "$REPO_ROOT/.env" -f "$REPO_ROOT/docker/compose.yaml")

case "${1:-up}" in
  up)
    "${COMPOSE[@]}" up -d
    echo "Waiting for PostgreSQL to accept connections..."
    for _ in $(seq 1 30); do
      if "${COMPOSE[@]}" exec -T postgres pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; then
        echo "PostgreSQL is ready on port ${POSTGRES_PORT}."
        exit 0
      fi
      sleep 1
    done
    echo "PostgreSQL did not become ready in time." >&2
    "${COMPOSE[@]}" logs --tail 30 postgres >&2
    exit 1
    ;;
  down)
    "${COMPOSE[@]}" down
    ;;
  reset)
    # Deletes the database volume: local development data only.
    "${COMPOSE[@]}" down -v
    ;;
  logs)
    "${COMPOSE[@]}" logs -f postgres
    ;;
  *)
    echo "Usage: $0 {up|down|reset|logs}" >&2
    exit 2
    ;;
esac
