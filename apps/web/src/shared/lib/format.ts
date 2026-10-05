const DISPLAY_TIME_ZONE = 'Asia/Kolkata';

/** Formats an ISO-8601 UTC timestamp for display in Asia/Kolkata. */
export function formatDateTime(isoTimestamp: string): string {
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: DISPLAY_TIME_ZONE,
  }).format(new Date(isoTimestamp));
}
