import { Navigate, Outlet } from "react-router";
import useAuthStore from "../stores/authStore";

export default function ProtectedRoute() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const user = useAuthStore((s) => s.user);
  const hasHydrated = useAuthStore((s) => s._hasHydrated);

  // Wait for Zustand to rehydrate from localStorage before making routing decisions.
  // Without this, a page refresh briefly shows null token and redirects to /login.
  if (!hasHydrated) {
    return null;
  }

  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  if (user?.mustChangePassword) {
    return <Navigate to="/change-password" replace />;
  }

  return <Outlet />;
}
