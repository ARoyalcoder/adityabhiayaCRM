import { Link } from 'react-router';

export function NotFoundPage() {
  return (
    <section className="rounded-lg border border-slate-200 bg-white p-6">
      <h1 className="text-lg font-semibold">Page not found</h1>
      <p className="mt-1 text-sm text-slate-600">This address does not exist in the application.</p>
      <Link className="mt-4 inline-block text-sm font-medium text-brand-700 underline" to="/">
        Back to the start page
      </Link>
    </section>
  );
}
