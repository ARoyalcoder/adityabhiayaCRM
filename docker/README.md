# docker/

| File | Purpose |
|------|---------|
| `compose.yaml` | Local development: PostgreSQL and Redis by default; with `--profile app` also the backend and frontend containers. Ports are bound to 127.0.0.1 only. |
| `compose.prod.yaml` | Production and staging topology: `postgres`, `redis`, `api`, `web` (Nginx). Reads every value from an environment file. |
| `api.Dockerfile` | Multi-stage build of the backend: Maven build and unit tests, then a JRE 21 runtime image with the layered jar, running as a non-root user. |
| `web.Dockerfile` | Multi-stage build of the frontend: Vite production build, then Nginx with the static files and configuration. |
| `nginx/` | Nginx configuration: TLS-ready server block, SPA fallback, `/api` proxying, security headers, rate limits. |
| `.env.prod.example` | Variable names for a deployed environment. The real file stays on the server, root-only. |

## Local

```bash
cp .env.example .env     # in the repository root
npm run infra:up         # PostgreSQL and Redis
npm run stack:up         # also the backend and frontend containers
npm run infra:down       # stops everything (data stays in the pgdata volume)
```

## Health checks

| Container | Check | Healthy when |
|-----------|-------|--------------|
| `postgres` | `pg_isready` | accepting connections |
| `redis` | `redis-cli ping` (authenticated) | answers `PONG` |
| `api` | `/actuator/health/readiness` | migrated, and PostgreSQL and Redis both answer |
| `web` | `/api/v1/health` through Nginx | serving, and the API behind it is up |

Each container starts only after the ones it depends on are healthy.

## Deployed

```bash
docker compose --env-file /etc/pawanputra/bos.env -f docker/compose.prod.yaml build
docker compose --env-file /etc/pawanputra/bos.env -f docker/compose.prod.yaml up -d
```

Flyway migrations run while the `api` container starts, before its readiness probe reports `UP`, and `web` waits for that probe. Redis runs without persistence: it holds only cache and short-lived counters.
