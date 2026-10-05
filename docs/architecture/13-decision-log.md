# 13. Decision Log

All architecture decisions in one place. Details and reasoning are in the linked documents. Status: **Proposed** until the owner accepts this architecture; afterwards a decision changes only by adding a new entry that supersedes the old one.

## Decisions

| Id | Area | Decision | Main reason | Doc |
|----|------|----------|-------------|-----|
| D-01.1 | System | Modular monolith, one deployable | Connected lifecycle, small team, local transactions | [01](01-system-architecture.md) |
| D-01.2 | System | Single company; verticals are a data dimension, not tenants | Shared customers, staff and money across verticals | [01](01-system-architecture.md) |
| D-01.3 | System | Client-rendered SPA served by Nginx, same origin as API | Internal tool, no SEO; enables strict cookies, no CORS | [01](01-system-architecture.md) |
| D-01.4 | System | REST only between browser and server; no WebSockets in v1 | No v1 workflow needs live push | [01](01-system-architecture.md) |
| D-02.1 | Modules | One public `api` package per module | Clear contract, everything else private | [02](02-module-boundaries.md) |
| D-02.2 | Modules | Direct calls upstream, events downstream; acyclic graph | Simplicity where natural, decoupling where needed | [02](02-module-boundaries.md) |
| D-02.3 | Modules | Vertical differences as configuration + validated JSONB attributes | One lifecycle, seven verticals, no code forks | [02](02-module-boundaries.md) |
| D-02.4 | Modules | `ai` and `inventory` reserved but not built | Implement only what is requested | [02](02-module-boundaries.md) |
| D-03.1 | Frontend | Feature folders mirroring backend modules | One vocabulary across the stack | [03](03-frontend-architecture.md) |
| D-03.2 | Frontend | TanStack Query is the only server-state store; no Redux | Removes hand-written caching | [03](03-frontend-architecture.md) |
| D-03.3 | Frontend | Hand-written typed API functions following OpenAPI | Stays within the agreed stack (see OD-3) | [03](03-frontend-architecture.md) |
| D-03.4 | Frontend | shadcn/ui components owned in the repo | Adjustable without forking a library | [03](03-frontend-architecture.md) |
| D-04.1 | Backend | Controller / use-case service / rich domain / repository per module | Rules live next to the data they protect | [04](04-backend-architecture.md) |
| D-04.2 | Backend | Hand-written DTO mappers; never expose entities | Explicit, safe, no extra library | [04](04-backend-architecture.md) |
| D-04.3 | Backend | Spring MVC + blocking JDBC, not reactive | Fits JPA and the workload | [04](04-backend-architecture.md) |
| D-04.4 | Backend | Maven build (see OD-7) | Common Spring default; stack names none | [04](04-backend-architecture.md) |
| D-05.1 | Database | One PostgreSQL database, one schema per module | Local transactions, visible ownership | [05](05-database-architecture.md) |
| D-05.2 | Database | UUID (v7) keys + human-readable document numbers | Safe ids, app-generated; numbers for people | [05](05-database-architecture.md) |
| D-05.3 | Database | Cross-module FKs allowed, but no cross-module JPA relations | Free integrity, private object models | [05](05-database-architecture.md) |
| D-05.4 | Database | Flyway SQL migrations, timestamped names, forward-only, `validate` | Reviewable, no collisions, safe startup | [05](05-database-architecture.md) |
| D-05.5 | Database | `numeric` money, status as `varchar` + `CHECK`, no hard delete of business documents | Exact money, easy evolution, traceability | [05](05-database-architecture.md) |
| D-05.6 | Database | Reporting through module-owned views + analytics materialised views | Cross-module reports without breaking ownership | [05](05-database-architecture.md) |
| D-05.7 | Database | PostgreSQL full-text + `pg_trgm` for search | No search engine needed at this scale | [05](05-database-architecture.md) |
| D-06.1 | API | REST under `/api/v1/{module}/...` | Lines up with modules and permissions | [06](06-api-architecture.md) |
| D-06.2 | API | Explicit lifecycle action endpoints | Each transition has its own permission, audit, events | [06](06-api-architecture.md) |
| D-06.3 | API | RFC 7807 Problem Details with stable error codes | Standard, built into Spring | [06](06-api-architecture.md) |
| D-06.4 | API | `Idempotency-Key` for money and webhooks, stored in PostgreSQL | Atomic with the payment | [06](06-api-architecture.md) |
| D-06.5 | API | `allowedActions` on resources | One source of truth for available buttons | [06](06-api-architecture.md) |
| D-07.1 | Security | Short-lived JWT access token in memory + rotating refresh token in `HttpOnly` `SameSite=Strict` cookie | XSS-resistant, stateless, CSRF-safe, mobile-ready | [07](07-security-architecture.md) |
| D-07.2 | Security | RBAC with admin-managed roles + data scopes (OWN/TEAM/VERTICAL/BRANCH/ALL) | Covers real cases, stays explainable | [07](07-security-architecture.md) |
| D-07.3 | Security | Out-of-scope records return 404 | Does not reveal existence | [07](07-security-architecture.md) |
| D-07.4 | Security | Audit written in the same transaction; append-only table | Audit cannot diverge from data | [07](07-security-architecture.md) |
| D-08.1 | Events | Spring events after commit for non-critical reactions | Already in stack, simple | [08](08-event-architecture.md) |
| D-08.2 | Events | Transactional outbox in PostgreSQL for must-happen reactions; at-least-once, idempotent listeners | No lost events without a broker | [08](08-event-architecture.md) |
| D-09.1 | Workflow | State machines in code; thresholds and recipients in configuration | Testable lifecycle, flexible rules | [09](09-workflow-architecture.md) |
| D-09.2 | Workflow | Generic approval engine with data-driven policies | One inbox, one audit, no hardcoded limits | [09](09-workflow-architecture.md) |
| D-09.3 | Workflow | Spring Scheduler jobs with PostgreSQL lease locks; idempotent batches | Safe with multiple instances, no extra library | [09](09-workflow-architecture.md) |
| D-09.4 | Workflow | Automation limited to fixed trigger/condition/action lists | Safe, testable business-defined rules | [09](09-workflow-architecture.md) |
| D-10.1 | Files | S3-compatible storage (MinIO self-hosted) + metadata in PostgreSQL | Keeps database small; portable between self-hosted and cloud | [10](10-file-storage-architecture.md) |
| D-10.2 | Files | Presigned direct upload/download; access decided by linked record | Large files bypass the app; consistent permissions | [10](10-file-storage-architecture.md) |
| D-10.3 | Files | Issued financial PDFs rendered once and stored | What the customer received never changes | [10](10-file-storage-architecture.md) |
| D-11.1 | Testing | Domain-heavy unit tests; Testcontainers with real infrastructure | Fast precise rules; production-faithful integration | [11](11-testing-architecture.md) |
| D-11.2 | Testing | REST Assured API suites with a permission matrix | Automatic security regression checks | [11](11-testing-architecture.md) |
| D-11.3 | Testing | Few Playwright journeys; k6 thresholds before release | Lifecycle proven end to end; performance checked | [11](11-testing-architecture.md) |
| D-12.1 | Deployment | Docker images + Docker Compose on one server; can add a second app instance | Right-sized operations for one company | [12](12-deployment-architecture.md) |
| D-12.2 | Deployment | Nginx as the only public entry; MinIO proxied behind it | Single TLS edge, app off the public network | [12](12-deployment-architecture.md) |
| D-12.3 | Deployment | Flyway on startup; expand/contract migrations | Safe rollback and future zero-downtime | [12](12-deployment-architecture.md) |
| D-12.4 | Deployment | Nightly base backup + WAL archiving; monthly restore drill | RPO ≤ 15 min, proven restores | [12](12-deployment-architecture.md) |

## Technologies deliberately not used

| Technology | Why not now | What would change that |
|-----------|-------------|------------------------|
| Microservices | Operational and consistency cost with no current benefit | A module with clearly different scaling or release needs |
| Kafka / RabbitMQ / Redis Streams | Outbox in PostgreSQL covers the volume | A module extracted into its own service |
| Workflow engine (Camunda, Flowable, Spring Statemachine) | Lifecycle is stable and simple enough for code | Business needs user-designed multi-step processes |
| Elasticsearch / OpenSearch | PostgreSQL search is enough at this volume | Millions of searchable documents or complex relevance needs |
| Kubernetes | One deployable, modest load | Many services or strong high-availability requirements |
| GraphQL | REST fits; per-field authorization is harder | Many very different clients with varied data needs |
| Redux / global client store | TanStack Query handles server state | Large amounts of complex client-only state |
| External identity provider | Built-in Spring Security covers the needs | Single sign-on with other company systems |
| WebSockets / SSE | No v1 live-push requirement | Live dispatch board or chat |

## Open decisions (need the owner's call)

These are outside the stack you named. Nothing here will be added unless you approve it. Each has a default that keeps the architecture working without it.

| Id | Question | Recommendation | Default if not approved |
|----|----------|----------------|------------------------|
| OD-1 | Add a test-only library to enforce module boundaries automatically? | **ArchUnit** (test scope only, nothing in production). Alternative: Spring Modulith, which also verifies boundaries but brings more opinions. | Boundaries checked in code review only. |
| OD-2 | Add a frontend unit test runner? | **Vitest** with Testing Library (Vite-native, no extra build config). | Frontend covered by type checks, lint and Playwright only. |
| OD-3 | Generate TypeScript API types from the OpenAPI document? | **openapi-typescript** as a dev-only tool. Removes hand-copied types and catches contract drift at compile time. | Types written by hand following OpenAPI. |
| OD-4 | Which library renders quotation/invoice PDFs? | **Thymeleaf HTML templates + OpenHTMLtoPDF**: templates are editable HTML/CSS, output is consistent. | PDFs postponed; documents viewed as web pages and printed from the browser. |
| OD-5 | Which CI service runs the quality gates? | **GitHub Actions**, since the repository is on GitHub. | Gates run manually before merge. |
| OD-6 | Where do metrics and logs go for dashboards and alerts? | Decide at first production deploy (self-hosted Prometheus + Grafana, or a hosted service). | Actuator metrics on the server, local log rotation. |
| OD-7 | Maven or Gradle? | **Maven** (already assumed in these docs). | Maven. |
| OD-8 | Hosting: own server with MinIO, or a cloud VM with managed S3 and PostgreSQL? | Start with one cloud VM running the Compose stack incl. MinIO; move storage/database to managed services when needed. Design supports either. | Single server with full Compose stack. |
| OD-9 | Email, SMS and WhatsApp providers? | Business choice (cost and DLT registration for Indian SMS). Each sits behind an adapter. | Email via SMTP only; in-app notifications. |
| OD-10 | Payment gateway for online collection? | Business choice. Webhook + idempotency design is ready for any provider. | Payments recorded manually (cash, cheque, bank transfer, UPI reference). |

## Assumptions to confirm

1. One legal company, possibly with branches; GST registration(s) per branch/state as configured.
2. Currency is INR only in v1; the money type carries currency so this can change.
3. Users are staff only in v1; no customer or vendor portal.
4. The lifecycle stage "Qualification" (from the project brief) is a configurable pipeline stage of the Opportunity rather than a separate record.
5. Field staff use phones with mobile data; offline use is not required in v1.
6. Expected scale: up to a few hundred users, tens of thousands of customers, hundreds of thousands of documents over several years.
