/** Response of GET /api/v1/system/info. */
export interface SystemInfo {
  application: string;
  version: string;
  /** ISO-8601 UTC timestamp. */
  serverTime: string;
}
