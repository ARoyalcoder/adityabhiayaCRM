import { z } from 'zod';
import type { SystemInfo } from '@pawanputra/shared-types';

export const systemInfoSchema = z.object({
  application: z.string().min(1),
  version: z.string().min(1),
  serverTime: z.iso.datetime({ offset: true }),
}) satisfies z.ZodType<SystemInfo>;
