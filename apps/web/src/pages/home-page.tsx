import { useSystemInfo } from '@/shared/api/system';
import { EmptyState, ErrorState, LoadingState } from '@/shared/components/states';
import { formatDateTime } from '@/shared/lib/format';

export function HomePage() {
  const { data, error, isPending, refetch } = useSystemInfo();

  return (
    <div className="space-y-4">
      <section>
        <h1 className="text-xl font-semibold">Scaffold check</h1>
        <p className="mt-1 text-sm text-slate-600">
          This page calls <code className="rounded bg-slate-200 px-1">GET /api/v1/system/info</code> to confirm the
          frontend reaches the backend on the same origin.
        </p>
      </section>

      {isPending ? <LoadingState label="Contacting the backend" /> : null}
      {error ? <ErrorState error={error} onRetry={() => void refetch()} /> : null}
      {data ? (
        <section className="rounded-lg border border-slate-200 bg-white p-6">
          <p className="text-sm font-medium text-green-800">Backend reachable</p>
          <dl className="mt-3 grid gap-2 text-sm sm:grid-cols-[10rem_1fr]">
            <dt className="text-slate-500">Application</dt>
            <dd className="font-medium">{data.application}</dd>
            <dt className="text-slate-500">Version</dt>
            <dd className="font-medium">{data.version}</dd>
            <dt className="text-slate-500">Server time (IST)</dt>
            <dd className="font-medium">{formatDateTime(data.serverTime)}</dd>
          </dl>
        </section>
      ) : null}

      <EmptyState
        title="No business modules yet"
        description="CRM, sales, projects, field operations, finance, service, workforce, vendors and analytics screens are added in later steps."
      />
    </div>
  );
}
