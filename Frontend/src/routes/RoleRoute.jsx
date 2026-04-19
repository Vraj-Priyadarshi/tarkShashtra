import { Navigate, Outlet } from "react-router";
import useAuthStore from "../stores/authStore";

export default function RoleRoute({ allowedRoles = [] }) {
  const user = useAuthStore((s) => s.user);

  const hasAccess = user?.roles?.some((role) => allowedRoles.includes(role));

  if (!hasAccess) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] text-center">
        <h1 className="text-4xl font-bold text-text-primary mb-2">403</h1>
        <p className="text-text-secondary text-lg">
          You don't have permission to access this page.
        </p>
      </div>
    );
  }

  return <Outlet />;
}
