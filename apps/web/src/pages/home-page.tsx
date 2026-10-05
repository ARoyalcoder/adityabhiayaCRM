import type { HealthStatus } from '@pawanputra/shared-types';
import { useApplicationHealth } from '@/shared/api/system';
import { EmptyState, ErrorState, LoadingState } from '@/shared/components/states';
import { formatDateTime } from '@/shared/lib/format';

const COMPONENT_LABELS: Record<string, string> = {
  database: 'Database (PostgreSQL)',
  redis: 'Cache (Redis)',
};

export function HomePage() {
  const { data, error, isPending, refetch, isFetching } = useApplicationHealth();

  return (
    <div className="space-y-4">
      <section>
        <h1 className="text-xl font-semibold">Environment check</h1>
        <p className="mt-1 text-sm text-slate-600">
          This page calls <code className="rounded bg-slate-200 px-1">GET /api/v1/health</code> to confirm the
          frontend reaches the backend, and the backend reaches its database and cache.
        </p>
      </section>

      {isPending ? <LoadingState label="Contacting the backend" /> : null}
      {error ? <ErrorState error={error} onRetry={() => void refetch()} /> : null}
      {data ? (
        <section className="rounded-lg border border-slate-200 bg-white p-6" aria-live="polite">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <p className="text-sm font-medium">
              {data.application} <span className="text-slate-500">v{data.version}</span>
            </p>
            <StatusBadge status={data.status} />
          </div>

          <ul className="mt-4 divide-y divide-slate-100 text-sm">
            {Object.entries(data.components).map(([name, status]) => (
              <li className="flex items-center justify-between py-2" key={name}>
                <span>{COMPONENT_LABELS[name] ?? name}</span>
                <StatusBadge status={status} />
              </li>
            ))}
          </ul>

          <div className="mt-4 flex flex-wrap items-center justify-between gap-2 text-xs text-slate-500">
            <span>Checked {formatDateTime(data.checkedAt)} IST</span>
            <button
              className="rounded-md border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 disabled:opacity-50"
              disabled={isFetching}
              onClick={() => void refetch()}
              type="button"
            >
              {isFetching ? 'Checking…' : 'Check again'}
            </button>
          </div>
        </section>
      ) : null}

      <EmptyState
        title="No business modules yet"
        description="CRM, sales, projects, field operations, finance, service, workforce, vendors and analytics screens are added in later steps."
      />
    </div>
  );
}

/** Colour is never the only signal: the status text is always shown. */
function StatusBadge({ status }: { status: HealthStatus }) {
  const styles =
    status === 'UP'
      ? 'bg-green-50 text-green-800 ring-green-600/20'
      : status === 'DOWN'
        ? 'bg-red-50 text-red-800 ring-red-600/20'
        : 'bg-amber-50 text-amber-800 ring-amber-600/20';
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ring-1 ring-inset ${styles}`}>{status}</span>
  );
}
