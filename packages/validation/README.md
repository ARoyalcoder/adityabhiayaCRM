# @pawanputra/validation

Zod schemas that mirror backend Bean Validation rules and API response shapes. Each schema `satisfies` its type from `@pawanputra/shared-types`, so the two cannot drift silently. The backend remains the authority for validation.

Consumed as TypeScript source by the frontend workspaces (no build step).
