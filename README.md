# Pawan Putra Business OS

A service-business operating system for a company running seven verticals (CCTV & Security, Digital Marketing, Interior Design, Architecture & Tech, Real Estate, Solar, IT Support). It covers the whole lifecycle of a job: enquiry → customer → opportunity → quotation → approval → order → project → delivery → invoice → payment → warranty → service → AMC → renewal → cross-sell.

**This repository currently contains the scaffold only.** No business module is implemented yet: the backend exposes one platform endpoint and the frontend has one page that calls it.

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
| Backend | Java 21, Spring Boot 3.5, Spring Data JPA, Bean Validation, Flyway, Actuator, springdoc OpenAPI |
| Database | PostgreSQL 16 |
| Infrastructure | Docker, Docker Compose, Nginx |
| Testing | JUnit 5, Testcontainers, REST Assured |

Architecture is a modular monolith. The reasoning behind every choice is in [`docs/architecture/`](docs/architecture/), which also lists the decisions still open.

## Requirements

Java 21, Node.js 22 or newer (see `.nvmrc`), Docker with Compose. Maven comes with the repository as the Maven Wrapper (`apps/api/mvnw`).

## Running it locally

```bash
cp .env.example .env     # then set POSTGRES_PASSWORD / DB_PASSWORD
npm install              # frontend workspaces

npm run db:up            # PostgreSQL in Docker
npm run dev:api          # backend on http://localhost:8080 (Flyway migrates on start)
npm run dev:web          # frontend on http://localhost:5173
```

Open http://localhost:5173. The start page shows the application name, version and server time read from the backend; that is the proof the frontend reaches the API.

The Vite dev server proxies `/api` to the backend, so the frontend always uses relative URLs and behaves exactly as it does in production behind Nginx.

| Address | What |
|---------|------|
| http://localhost:5173 | Frontend |
| http://localhost:8080/api/v1/system/info | Application name, version, server time |
| http://localhost:8080/api/v1/docs | OpenAPI UI (not served in production) |
| http://localhost:8080/actuator/health | Health; `/readiness` and `/liveness` for probes |

## Commands

| Command | Does |
|---------|------|
| `npm run db:up` / `db:down` / `db:logs` | Local PostgreSQL container |
| `npm run dev:api` | Backend with the `local` profile |
| `npm run dev:web` | Frontend dev server |
| `npm run lint` | ESLint across the frontend workspaces |
| `npm run typecheck` | TypeScript across the frontend workspaces |
| `npm run build:web` | Production build of the SPA |
| `npm run test:api` | Backend unit and integration tests (integration tests need Docker) |
| `npm run verify` | Everything above that checks the code |

## Configuration

No secret is committed. `.env.example` lists the variables used locally; `docker/.env.prod.example` lists the ones a deployed environment needs. Copy each to a file that is not in version control (`.env` locally, a root-only file on the server).

The backend reads only technical settings from `application.yml`. Configurable business rules (approval limits, payment terms, numbering formats and so on) will live in the database, never in source.

## Deployed environments

`docker/compose.prod.yaml` describes the production and staging topology: `postgres`, `api` and `web` (Nginx serving the SPA and proxying `/api`). Nginx is the only container published on the host. Flyway migrations run while the `api` container starts, before its readiness probe reports `UP`.

```bash
docker compose --env-file /etc/pawanputra/bos.env -f docker/compose.prod.yaml build
docker compose --env-file /etc/pawanputra/bos.env -f docker/compose.prod.yaml up -d
```

See [`docker/README.md`](docker/README.md) and [`docs/architecture/12-deployment-architecture.md`](docs/architecture/12-deployment-architecture.md).

## Documentation

- [`docs/architecture/`](docs/architecture/) — system, module, frontend, backend, database, API, security, event, workflow, storage, testing and deployment architecture, plus the decision log.
- [`docker/README.md`](docker/README.md) — containers and images.
- [`scripts/README.md`](scripts/README.md) — development scripts.
