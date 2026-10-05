# docker/

| File | Purpose |
|------|---------|
| `compose.yaml` | Local development infrastructure (PostgreSQL only). Backend and frontend run on the host with hot reload. |
| `compose.prod.yaml` | Production and staging topology: `postgres`, `api`, `web` (Nginx). Reads every value from an environment file. |
| `api.Dockerfile` | Multi-stage build of the backend: Maven build and unit tests, then a JRE 21 runtime image with the layered jar, running as a non-root user. |
| `web.Dockerfile` | Multi-stage build of the frontend: Vite production build, then Nginx with the static files and configuration. |
| `nginx/` | Nginx configuration: TLS-ready server block, SPA fallback, `/api` proxying, security headers, rate limits. |
| `.env.prod.example` | Variable names for a deployed environment. The real file stays on the server, root-only. |

## Local

```bash
cp .env.example .env     # in the repository root
npm run db:up            # starts PostgreSQL
npm run db:down          # stops it (data stays in the pgdata volume)
```

## Deployed

```bash
docker compose --env-file /etc/pawanputra/bos.env -f docker/compose.prod.yaml build
docker compose --env-file /etc/pawanputra/bos.env -f docker/compose.prod.yaml up -d
```

Flyway migrations run while the `api` container starts, before its readiness probe reports `UP`, and `web` waits for that probe.
