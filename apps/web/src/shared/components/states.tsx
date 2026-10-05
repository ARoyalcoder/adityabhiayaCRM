import type { ReactNode } from 'react';
import { ApiError } from '@/shared/api';

/**
 * Loading, empty and error states shared by every data-driven screen
 * (project rule 7, docs/architecture/03-frontend-architecture.md).
 */

export function LoadingState({ label = 'Loading' }: { label?: string }) {
  return (
    <div className="animate-none rounded-lg border border-slate-200 bg-white p-6" aria-busy="true">
      <p className="text-sm text-slate-600">{label}…</p>
      <div className="mt-3 space-y-2" aria-hidden="true">
        <div className="h-3 w-2/3 rounded bg-slate-200" />
        <div className="h-3 w-1/2 rounded bg-slate-200" />
      </div>
    </div>
  );
}

export function EmptyState({ title, description, action }: { title: string; description: string; action?: ReactNode }) {
  return (
    <div className="rounded-lg border border-dashed border-slate-300 bg-white p-6 text-center">
      <p className="text-sm font-medium text-slate-900">{title}</p>
      <p className="mt-1 text-sm text-slate-600">{description}</p>
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  );
}

export function ErrorState({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  const message = error instanceof Error ? error.message : 'Unexpected error';
  const requestId = error instanceof ApiError ? error.requestId : undefined;

  return (
    <div className="rounded-lg border border-red-200 bg-red-50 p-6" role="alert">
      <p className="text-sm font-medium text-red-900">Could not load this information</p>
      <p className="mt-1 text-sm text-red-800">{message}</p>
      {requestId ? <p className="mt-1 text-xs text-red-700">Request id: {requestId}</p> : null}
      {onRetry ? (
        <button
          className="mt-4 rounded-md bg-red-700 px-3 py-1.5 text-sm font-medium text-white"
          onClick={onRetry}
          type="button"
        >
          Try again
        </button>
      ) : null}
    </div>
  );
}
