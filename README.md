# Pawan Putra Business OS

A service-business operating system for a company running seven verticals (CCTV & Security, Digital Marketing, Interior Design, Architecture & Tech, Real Estate, Solar, IT Support). It covers the whole lifecycle of a job: enquiry → customer → opportunity → quotation → approval → order → project → delivery → invoice → payment → warranty → service → AMC → renewal → cross-sell.

**This repository currently contains the scaffold and the local development environment only.** No business module and no authentication is implemented yet: the backend exposes platform endpoints (identity and health) and the frontend has one page that shows the health of the running environment.

## Layout

```
apps/
  api/        Spring Boot backend (Java 21, Maven)
  web/        React SPA (TypeScript, Vite)
packages/
  shared-types/   TypeScript types for the /api/v1 contract
  validation/     Zod schemas mirroring backend validation
docker/       Compose files, Dockerfiles, Nginx configuration
docs/         Architecture documentation
scripts/      Development scripts
```

The frontend workspaces are npm workspaces, declared in the root `package.json`; the backend is a separate Maven project under `apps/api`.

## Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 19, TypeScript, Vite, React Router, TanStack Query, Zod, Tailwind CSS |
| Backend | Java 21, Spring Boot 3.5, Spring Data JPA, Spring Data Redis, Bean Validation, Flyway, Actuator, springdoc OpenAPI, structured JSON logging (ECS) |
| Database | PostgreSQL 16 |
| Cache | Redis 7 (cache and short-lived counters only) |
| Infrastructure | Docker, Docker Compose, Nginx |
| Testing | JUnit 5, Testcontainers, REST Assured |

Architecture is a modular monolith. The reasoning behind every choice is in [`docs/architecture/`](docs/architecture/), which also lists the decisions still open.

## Requirements

Java 21, Node.js 22 or newer (see `.nvmrc`), Docker with Compose. Maven comes with the repository as the Maven Wrapper (`apps/api/mvnw`).

## Running it locally

```bash
cp .env.example .env     # then set the passwords
npm install              # frontend workspaces

npm run infra:up         # PostgreSQL and Redis in Docker, waits until both are healthy
npm run dev:api          # backend on http://localhost:8080 (Flyway migrates on start)
npm run dev:web          # frontend on http://localhost:5173
npm run check:services   # smoke-tests every service and the browser path
```

Open http://localhost:5173. The start page shows the overall status and the status of the database and Redis, read from `GET /api/v1/health`, which proves the whole chain works: browser → frontend → backend → PostgreSQL and Redis.

The Vite dev server proxies `/api` to the backend, so the frontend always uses relative URLs and behaves exactly as it does in production behind Nginx.

To run everything in containers instead (the same images as production), use `npm run stack:up`; the frontend is then on http://localhost:8081 and `bash scripts/check-services.sh --stack` checks it. Stop the host backend first, since both use port 8080.

| Address | What |
|---------|------|
| http://localhost:5173 | Frontend |
| http://localhost:8080/api/v1/health | Overall health plus `database` and `redis` status; `503` when any is down |
| http://localhost:8080/api/v1/system/info | Application name, version, server time |
| http://localhost:8080/api/v1/docs | OpenAPI UI (not served in production) |
| http://localhost:8080/actuator/health/readiness | Readiness probe: database and Redis (internal only) |
| http://localhost:8080/actuator/health/liveness | Liveness probe: the process only (internal only) |

## Configuration

| Profile | Used for | Where |
|---------|----------|-------|
| `local` | Development on a laptop | `application-local.yml`; values from the root `.env` |
| `test` | Automated tests | `src/test/resources/application-test.yml`; PostgreSQL and Redis come from Testcontainers |
| `staging`, `prod` | Deployed environments | `application-staging.yml`, `application-prod.yml`; values from the server's environment file |

Logs are one JSON object per line on stdout (Elastic Common Schema), with the request id in `requestId`, in every profile. `LOG_FORMAT` switches the schema to `logstash` or `gelf`.

## Commands

| Command | Does |
|---------|------|
| `npm run infra:up` / `infra:down` / `infra:status` / `infra:logs` | Local PostgreSQL and Redis containers |
| `npm run infra:reset` | Stops everything and deletes the local database volume |
| `npm run stack:up` | PostgreSQL, Redis, backend and frontend, all in containers |
| `npm run dev:api` | Backend with the `local` profile |
| `npm run dev:web` | Frontend dev server |
| `npm run check:services` | Smoke test of every running service |
| `npm run lint` | ESLint across the frontend workspaces |
| `npm run typecheck` | TypeScript across the frontend workspaces |
| `npm run build:web` | Production build of the SPA |
| `npm run test:api` | Backend unit and integration tests (integration tests need Docker) |
| `npm run verify` | Lint, type-check, build and all backend tests |

## Secrets

No secret is committed. `.env.example` lists the variables used locally; `docker/.env.prod.example` lists the ones a deployed environment needs. Copy each to a file that is not in version control (`.env` locally, a root-only file on the server).

The backend reads only technical settings from `application.yml`. Configurable business rules (approval limits, payment terms, numbering formats and so on) will live in the database, never in source.

## Deployed environments

`docker/compose.prod.yaml` describes the production and staging topology: `postgres`, `redis`, `api` and `web` (Nginx serving the SPA and proxying `/api`). Every container has a health check. Nginx is the only container published on the host. Flyway migrations run while the `api` container starts, before its readiness probe reports `UP`.

```bash
docker compose --env-file /etc/pawanputra/bos.env -f docker/compose.prod.yaml build
docker compose --env-file /etc/pawanputra/bos.env -f docker/compose.prod.yaml up -d
```

See [`docker/README.md`](docker/README.md) and [`docs/architecture/12-deployment-architecture.md`](docs/architecture/12-deployment-architecture.md).

## Documentation

- [`docs/architecture/`](docs/architecture/) — system, module, frontend, backend, database, API, security, event, workflow, storage, testing and deployment architecture, plus the decision log.
- [`docker/README.md`](docker/README.md) — containers and images.
- [`scripts/README.md`](scripts/README.md) — development scripts.
