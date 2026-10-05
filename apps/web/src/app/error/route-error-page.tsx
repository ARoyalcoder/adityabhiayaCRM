import { isRouteErrorResponse, useRouteError } from 'react-router';

export function RouteErrorPage() {
  const error = useRouteError();
  const message = isRouteErrorResponse(error)
    ? `${error.status} ${error.statusText}`
    : error instanceof Error
      ? error.message
      : 'Unexpected error';

  return (
    <div className="mx-auto max-w-5xl px-4 py-6">
      <section className="rounded-lg border border-red-200 bg-red-50 p-6" role="alert">
        <h1 className="text-lg font-semibold text-red-900">Something went wrong</h1>
        <p className="mt-1 text-sm text-red-800">{message}</p>
        <button
          className="mt-4 rounded-md bg-red-700 px-3 py-1.5 text-sm font-medium text-white"
          onClick={() => window.location.reload()}
          type="button"
        >
          Reload
        </button>
      </section>
    </div>
  );
}
