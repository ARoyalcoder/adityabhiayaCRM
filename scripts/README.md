# scripts/

Thin wrappers so a new developer needs one command per task. Each reads the repository-root `.env`.

| Script | npm script | Does |
|--------|-----------|------|
| `dev-infra.sh up\|stack\|status\|logs\|down\|reset` | `npm run infra:up` / `stack:up` / `infra:status` / `infra:logs` / `infra:down` / `infra:reset` | Local containers. `up` starts PostgreSQL and Redis, `stack` also the backend and frontend; both wait for the health checks. `reset` deletes the database volume. |
| `dev-api.sh` | `npm run dev:api` | Runs the backend with the `local` profile; Flyway migrates on startup. |
| `check-services.sh [--stack]` | `npm run check:services` | Smoke test: PostgreSQL, Redis, backend readiness, `GET /api/v1/health`, the frontend, and the frontend-to-backend path. |
| `test-api.sh` | `npm run test:api` | Backend unit and integration tests (integration tests need Docker). |
| `verify.sh` | `npm run verify` | Frontend lint, type-check and build, then the backend tests. |

The frontend dev server is `npm run dev:web` from the repository root.
