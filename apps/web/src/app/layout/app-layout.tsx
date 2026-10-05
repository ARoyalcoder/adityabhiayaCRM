import { Outlet } from 'react-router';

export function AppLayout() {
  return (
    <div className="flex min-h-dvh flex-col">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center gap-3 px-4 py-3">
          <span
            aria-hidden="true"
            className="grid size-8 place-items-center rounded-md bg-brand-600 text-sm font-bold text-white"
          >
            PP
          </span>
          <div>
            <p className="text-sm font-semibold">Pawan Putra Business OS</p>
            <p className="text-xs text-slate-500">Service business operating system</p>
          </div>
        </div>
      </header>
      <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-6">
        <Outlet />
      </main>
      <footer className="border-t border-slate-200 bg-white">
        <p className="mx-auto max-w-5xl px-4 py-3 text-xs text-slate-500">
          Scaffold only. No business modules are implemented yet.
        </p>
      </footer>
    </div>
  );
}
