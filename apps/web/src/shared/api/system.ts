import { useQuery } from '@tanstack/react-query';
import { systemInfoSchema } from '@pawanputra/validation';
import type { SystemInfo } from '@pawanputra/shared-types';
import { apiRequest } from './client';

export const systemKeys = {
  info: () => ['system', 'info'] as const,
};

export function fetchSystemInfo(signal?: AbortSignal): Promise<SystemInfo> {
  return apiRequest('/system/info', systemInfoSchema, { signal });
}

/** Proves the browser can reach the backend through the same-origin /api path. */
export function useSystemInfo() {
  return useQuery({
    queryKey: systemKeys.info(),
    queryFn: ({ signal }) => fetchSystemInfo(signal),
  });
}
