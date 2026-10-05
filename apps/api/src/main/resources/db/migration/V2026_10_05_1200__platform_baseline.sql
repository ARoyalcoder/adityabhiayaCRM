-- Baseline of the platform schema.
--
-- Each module owns one PostgreSQL schema of the same name and creates it in its
-- own first migration (docs/architecture/05-database-architecture.md, D-05.1).
-- This migration creates only the platform schema, which holds technical tables
-- (outbox, idempotency keys, scheduler locks) added in later steps. No business
-- table exists yet.

CREATE SCHEMA IF NOT EXISTS platform;

COMMENT ON SCHEMA platform IS
    'Technical tables owned by the platform layer: outbox, idempotency keys, scheduler locks.';
