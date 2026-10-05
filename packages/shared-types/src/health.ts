/** Status values reported by GET /api/v1/health (Spring Boot health statuses). */
export type HealthStatus = 'UP' | 'DOWN' | 'OUT_OF_SERVICE' | 'UNKNOWN';

/** Response of GET /api/v1/health. Returned with 200 when UP, 503 otherwise. */
export interface ApplicationHealth {
  status: HealthStatus;
  application: string;
  version: string;
  /** ISO-8601 UTC timestamp. */
  checkedAt: string;
  /** Status per dependency, e.g. { database: 'UP', redis: 'UP' }. */
  components: Record<string, HealthStatus>;
}
