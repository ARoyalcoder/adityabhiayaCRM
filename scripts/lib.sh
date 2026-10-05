# Shared helpers for the development scripts. Sourced, not executed.

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Loads the repository-root .env so every script and the compose files agree on
# ports, database name and credentials.
load_env() {
  if [ ! -f "$REPO_ROOT/.env" ]; then
    echo "No .env found. Copy .env.example to .env first:" >&2
    echo "  cp .env.example .env" >&2
    exit 1
  fi
  set -a
  # shellcheck disable=SC1091
  . "$REPO_ROOT/.env"
  set +a
}

require() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Required command not found: $1" >&2
    exit 1
  }
}
