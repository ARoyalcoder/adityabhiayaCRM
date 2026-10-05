# scripts/

Thin wrappers so a new developer needs one command per task. Each reads the repository-root `.env`.

| Script | npm script | Does |
|--------|-----------|------|
| `dev-db.sh up\|down\|reset\|logs` | `npm run db:up` / `db:down` / `db:logs` | Local PostgreSQL container. `reset` also deletes its volume. |
| `dev-api.sh` | `npm run dev:api` | Runs the backend with the `local` profile; Flyway migrates on startup. |
| `test-api.sh` | `npm run test:api` | Backend unit and integration tests (integration tests need Docker). |
| `verify.sh` | `npm run verify` | Everything: frontend lint, type-check and build, then the backend tests. |

The frontend dev server is `npm run dev:web` from the repository root.
