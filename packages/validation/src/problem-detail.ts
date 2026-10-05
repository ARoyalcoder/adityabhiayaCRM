import { z } from 'zod';
import type { ProblemDetail } from '@pawanputra/shared-types';

export const problemDetailSchema = z.looseObject({
  type: z.string().optional(),
  title: z.string().optional(),
  status: z.number().int().optional(),
  detail: z.string().optional(),
  instance: z.string().optional(),
  requestId: z.string().optional(),
}) satisfies z.ZodType<ProblemDetail>;
