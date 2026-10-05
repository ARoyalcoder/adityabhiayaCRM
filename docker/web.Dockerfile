# syntax=docker/dockerfile:1

# Stage 1: build the SPA.
FROM node:22-alpine AS build
WORKDIR /build

COPY package.json package-lock.json ./
COPY apps/web/package.json ./apps/web/
COPY packages/shared-types/package.json ./packages/shared-types/
COPY packages/validation/package.json ./packages/validation/
RUN npm ci

COPY apps/web ./apps/web
COPY packages ./packages
RUN npm run build --workspace @pawanputra/web

# Stage 2: Nginx serving the static files and proxying /api to the API container.
FROM nginx:1.27-alpine AS runtime
COPY docker/nginx/nginx.conf /etc/nginx/nginx.conf
COPY docker/nginx/app.conf /etc/nginx/conf.d/default.conf
COPY docker/nginx/proxy-headers.inc /etc/nginx/conf.d/proxy-headers.inc
COPY docker/nginx/security-headers.inc /etc/nginx/conf.d/security-headers.inc
COPY --from=build /build/apps/web/dist /usr/share/nginx/html
EXPOSE 80
