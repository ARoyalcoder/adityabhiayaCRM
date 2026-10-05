import { problemDetailSchema } from '@pawanputra/validation';
import type { ProblemDetail } from '@pawanputra/shared-types';

/** An error carrying the server's RFC 7807 body, or a transport failure. */
export class ApiError extends Error {
  readonly status: number;
  readonly problem: ProblemDetail | undefined;
  readonly requestId: string | undefined;

  constructor(status: number, message: string, problem?: ProblemDetail, requestId?: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.problem = problem;
    this.requestId = requestId;
  }
}

export async function toApiError(response: Response): Promise<ApiError> {
  const requestId = response.headers.get('X-Request-Id') ?? undefined;
  let problem: ProblemDetail | undefined;

  try {
    const parsed = problemDetailSchema.safeParse(await response.json());
    if (parsed.success) {
      problem = parsed.data;
    }
  } catch {
    // Body was empty or not JSON; the status alone describes the failure.
  }

  const message = problem?.detail ?? problem?.title ?? `Request failed with status ${response.status}`;
  return new ApiError(response.status, message, problem, problem?.requestId ?? requestId);
}
