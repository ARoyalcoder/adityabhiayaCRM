import { z } from 'zod';
import type { ApplicationHealth } from '@pawanputra/shared-types';

export const healthStatusSchema = z.enum(['UP', 'DOWN', 'OUT_OF_SERVICE', 'UNKNOWN']);

export const applicationHealthSchema = z.object({
  status: healthStatusSchema,
  application: z.string().min(1),
  version: z.string().min(1),
  checkedAt: z.iso.datetime({ offset: true }),
  components: z.record(z.string(), healthStatusSchema),
}) satisfies z.ZodType<ApplicationHealth>;
