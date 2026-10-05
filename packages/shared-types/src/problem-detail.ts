/**
 * RFC 7807 Problem Details, the error body of every /api/v1 endpoint
 * (docs/architecture/06-api-architecture.md).
 */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  /** Request id for support, also returned in the X-Request-Id header. */
  requestId?: string;
  [extension: string]: unknown;
}
