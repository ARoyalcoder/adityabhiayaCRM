import { useQuery } from '@tanstack/react-query';
import { applicationHealthSchema } from '@pawanputra/validation';
import type { ApplicationHealth } from '@pawanputra/shared-types';
import { apiRequest } from './client';

export const systemKeys = {
  health: () => ['system', 'health'] as const,
};

/** GET /api/v1/health. A 503 still carries the per-component statuses. */
export function fetchApplicationHealth(signal?: AbortSignal): Promise<ApplicationHealth> {
  return apiRequest('/health', applicationHealthSchema, { signal, acceptStatuses: [503] });
}

/** Proves the browser reaches the backend, and the backend its database and Redis. */
export function useApplicationHealth() {
  return useQuery({
    queryKey: systemKeys.health(),
    queryFn: ({ signal }) => fetchApplicationHealth(signal),
    refetchInterval: 30_000,
  });
}
