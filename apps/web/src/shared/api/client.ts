import type { z } from 'zod';
import { ApiError, toApiError } from './problem-detail';

/**
 * The API is always same-origin: Nginx serves the SPA and proxies /api in
 * deployed environments, and the Vite dev server proxies it locally
 * (docs/architecture/01-system-architecture.md, D-01.3).
 */
const API_BASE_PATH = '/api/v1';

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PATCH' | 'PUT' | 'DELETE';
  body?: unknown;
  signal?: AbortSignal;
  /** Required for money operations and webhooks (D-06.4). */
  idempotencyKey?: string;
  /**
   * Non-2xx statuses whose body is a normal response rather than a Problem
   * Detail, e.g. 503 from the health endpoint, which still reports components.
   */
  acceptStatuses?: readonly number[];
}

/**
 * Performs a request and validates the response against `schema`, so a change
 * in the API contract fails here rather than deep inside a component.
 */
export async function apiRequest<T>(
  path: string,
  schema: z.ZodType<T>,
  options: RequestOptions = {},
): Promise<T> {
  const { method = 'GET', body, signal, idempotencyKey, acceptStatuses = [] } = options;

  const headers = new Headers({ Accept: 'application/json' });
  if (body !== undefined) {
    headers.set('Content-Type', 'application/json');
  }
  if (idempotencyKey) {
    headers.set('Idempotency-Key', idempotencyKey);
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_PATH}${path}`, {
      method,
      headers,
      credentials: 'same-origin',
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    });
  } catch {
    // Network failure or aborted request: no HTTP status exists, so 0 marks it.
    throw new ApiError(0, 'The server could not be reached.');
  }

  if (!response.ok && !acceptStatuses.includes(response.status)) {
    throw await toApiError(response);
  }

  if (response.status === 204) {
    return schema.parse(undefined);
  }

  return schema.parse(await response.json());
}
