import { createBrowserRouter, RouterProvider } from 'react-router';
import { AppLayout } from '@/app/layout/app-layout';
import { NotFoundPage } from '@/app/error/not-found-page';
import { RouteErrorPage } from '@/app/error/route-error-page';
import { HomePage } from '@/pages/home-page';

/**
 * Business module routes are added here as each module is built; each module
 * will own a lazy-loaded routes file (docs/architecture/03-frontend-architecture.md).
 */
const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    errorElement: <RouteErrorPage />,
    children: [
      { index: true, element: <HomePage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);

export function AppRouter() {
  return <RouterProvider router={router} />;
}
