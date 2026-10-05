# apps/web

React + TypeScript + Vite single-page application for Pawan Putra Business OS.

- `src/app/` application shell: providers, router, layout, error pages
- `src/shared/` HTTP client, shared state components, formatting helpers
- `src/pages/` routes that are not owned by a business module
- `src/modules/` one folder per business module (added as modules are built)

Structure and boundary rules: `docs/architecture/03-frontend-architecture.md`.

## Commands

Run from the repository root (npm workspaces):

```bash
npm run dev:web      # Vite dev server on http://localhost:5173, proxying /api
npm run build:web    # type-check and production build into apps/web/dist
npm run lint         # ESLint across all workspaces
npm run typecheck    # tsc across all workspaces
```

`/api` requests are proxied to `API_PROXY_TARGET` (default `http://localhost:8080`), so the application always uses relative URLs, exactly as in production behind Nginx.
