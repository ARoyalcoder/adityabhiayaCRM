# 06. API Architecture

## Style

**Decision.** JSON over HTTPS, resource-oriented REST, all under `/api/v1`, documented with OpenAPI generated from the code.

**Why.** REST is what every tool in the stack expects (TanStack Query, springdoc OpenAPI, REST Assured, k6), it is cacheable and debuggable with ordinary tools, and it fits a CRUD-plus-workflow domain. One version prefix lets us introduce `/api/v2` for a breaking change without breaking existing clients (e.g. a future mobile app).

**Rejected.** *GraphQL* (not in the stack; adds server complexity and makes per-field authorization and query-cost control harder). *RPC-only endpoints* (lose HTTP semantics for caching, status codes and tooling).

## URL design

```
/api/v1/{module}/{resource}[/{id}[/{sub-resource}|/{action}]]
```

| Purpose | Example |
|---------|---------|
| List / create | `GET, POST /api/v1/crm/leads` |
| Read / update | `GET, PATCH /api/v1/crm/leads/{id}` |
| Sub-resource | `GET, POST /api/v1/crm/customers/{id}/contacts` |
| Lifecycle action | `POST /api/v1/crm/leads/{id}/convert` |
| Lifecycle action | `POST /api/v1/sales/quotations/{id}/versions/{versionId}/submit` |
| Lifecycle action | `POST /api/v1/sales/quotations/{id}/accept` |
| Lifecycle action | `POST /api/v1/finance/invoices/{id}/issue` |
| Record payment | `POST /api/v1/finance/payments` (requires `Idempotency-Key`) |
| Approval decision | `POST /api/v1/approval/requests/{id}/approve` |
| Webhook | `POST /api/v1/webhooks/payments/{provider}` |
| Files | `POST /api/v1/documents/uploads` (returns presigned URL) |

Rules:
- Plural nouns, kebab-case paths, camelCase JSON fields.
- The module prefix matches the backend module and the permission namespace, so routing, ownership and authorization line up.
- **Status changes are explicit action endpoints, never a `PATCH status=...`.** *Why:* each transition has its own permission, validation, audit entry and events (`submit`, `approve`, `accept`, `cancel`). A generic status field update would bypass all of that.
- `PATCH` updates editable fields only and carries the `version` read by the client.
- `DELETE` is used only for records that are allowed to disappear (e.g. a draft line). Business documents are cancelled or voided through actions.

## Request and response conventions

### Single resource
```json
{
  "id": "0192f0c8-6f1a-7b9e-9a51-3c8e2c4d1a77",
  "number": "QT-SOLR-2026-00042",
  "status": "PENDING_APPROVAL",
  "verticalId": "...",
  "customer": { "id": "...", "name": "Sharma Residency" },
  "total": { "amount": "248500.00", "currency": "INR" },
  "attributes": { "systemSizeKw": "5.4", "roofType": "RCC" },
  "version": 3,
  "createdAt": "2026-10-05T07:15:00Z",
  "allowedActions": ["approve", "reject"]
}
```

- **Money** is `{ "amount": "<decimal string>", "currency": "INR" }` so JavaScript never rounds it.
- **Timestamps** ISO-8601 UTC; **dates** `YYYY-MM-DD`.
- **`allowedActions`** lists the transitions this user may perform on this record right now (computed from state machine + permissions + data scope). *Why:* the UI shows correct buttons without duplicating the rules; the server still re-checks on the action call.
- Embedded summaries (`customer.name`) are read-only conveniences; writes take ids.

### Lists and paging
```
GET /api/v1/crm/leads?status=OPEN&verticalId=...&q=sharma&sort=createdAt,desc&page=0&size=25
```
```json
{
  "items": [ ... ],
  "page": 0, "size": 25, "totalItems": 312, "totalPages": 13
}
```
- Offset paging (page/size) for back-office lists, where users jump to pages and need totals. Maximum `size` is 100.
- Keyset (cursor) paging for large append-only feeds (activity timeline, audit log), returned as `{ items, nextCursor }`. *Why both:* offset is natural for tables; keyset stays fast on very large, constantly growing feeds.
- Filters are explicit, documented query parameters per endpoint. No free-form query language.

## Errors: RFC 7807 Problem Details

Spring's built-in `ProblemDetail` support is used for every error.

```json
{
  "type": "https://errors.pawanputra.local/validation",
  "title": "Validation failed",
  "status": 422,
  "detail": "2 fields are invalid",
  "instance": "/api/v1/crm/leads",
  "requestId": "b6f1c0e2...",
  "errors": [
    { "field": "phone", "code": "INVALID_FORMAT", "message": "Enter a valid 10-digit mobile number" },
    { "field": "attributes.systemSizeKw", "code": "REQUIRED", "message": "System size is required for Solar" }
  ]
}
```

| Status | When |
|--------|------|
| 400 | Malformed request (bad JSON, wrong types) |
| 401 | Not authenticated / token expired |
| 403 | Authenticated but lacks permission or data scope |
| 404 | Not found **or not visible to this user** (avoids leaking existence) |
| 409 | Version conflict, invalid state transition, duplicate idempotency key with different payload |
| 422 | Validation or business-rule failure (with field errors and stable `code`s) |
| 429 | Rate limited |
| 500 | Unexpected; generic message plus `requestId` |

**Why stable error codes.** The UI maps `code` to field messages and future clients can react programmatically; message text can change freely.

## Idempotency

**Decision.** Endpoints that move money or are called by external systems require an `Idempotency-Key` header: recording payments, refunds, issuing invoices, payment-gateway webhooks, and any `POST` a client may retry automatically.

How it works:
1. The key (a client-generated UUID, or the provider's event id for webhooks) plus the user and endpoint is stored in `platform.idempotency_key` **in the same transaction** as the business change, together with a hash of the request and the response.
2. A retry with the same key and same payload returns the stored response without re-executing.
3. The same key with a different payload returns `409`.
4. Keys expire after a configured period (default 7 days) and are cleaned up by a scheduled job.

**Why in PostgreSQL, not Redis.** The key must commit atomically with the payment. If it lived in Redis, a crash between the two writes could record a payment twice or lose the guard. Redis is fast but not transactional with our data.

## Webhooks (inbound)

- One endpoint per provider under `/api/v1/webhooks/...`, excluded from user authentication but **verified by provider signature** (shared secret / HMAC from environment config).
- The handler validates the signature, stores the raw event with its provider event id (idempotency), returns `2xx` quickly, and processes the event asynchronously through the outbox relay. *Why:* providers retry on slow or failed responses; fast acknowledgement plus idempotent processing makes retries harmless.

## Versioning and compatibility

- Additive changes (new optional field, new endpoint) do not change the version.
- Removing or renaming fields, or changing meaning, requires a new version of that endpoint under `/api/v2`, with the old one kept until clients move.
- Clients must ignore unknown fields.

## OpenAPI

- Generated from controllers and DTO annotations with springdoc (the standard OpenAPI integration for Spring Boot), served as JSON and Swagger UI in dev and staging, disabled or protected in production.
- Every endpoint documents: required permission, possible error codes, and whether `Idempotency-Key` is required.
- The OpenAPI document is the contract the frontend's typed API functions and Zod schemas follow ([03](03-frontend-architecture.md)).

## Rate limiting and payload limits

- Nginx applies per-IP rate limits on `/api/v1/auth/*` and webhooks, and a general limit on `/api` ([12](12-deployment-architecture.md)).
- Login attempts are additionally throttled per account in Redis ([07](07-security-architecture.md)).
- Request bodies are limited to 1 MB at Nginx; files never go through the API ([10](10-file-storage-architecture.md)).

## Decisions

| Id | Decision | Main reason |
|----|----------|------------|
| D-06.1 | REST under `/api/v1` with module prefixes | Fits stack and domain; lines up with modules and permissions |
| D-06.2 | Explicit lifecycle action endpoints | Each transition gets its own permission, audit and events |
| D-06.3 | Problem Details with stable error codes | Standard, built into Spring, machine-readable |
| D-06.4 | Idempotency keys stored in PostgreSQL | Atomic with the money movement |
| D-06.5 | `allowedActions` on resources | One source of truth for which buttons appear |
