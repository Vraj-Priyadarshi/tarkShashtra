import { useCallback } from "react";
import { useNavigate } from "react-router";
import useAuthStore from "../stores/authStore";
import { login as loginApi, changePassword as changePasswordApi } from "../api/auth";

export default function useAuth() {
  const navigate = useNavigate();
  const {
    user,
    accessToken,
    login: storeLogin,
    logout: storeLogout,
    hasRole,
    getPrimaryRole,
    activeView,
    setActiveView,
  } = useAuthStore();

  const isAuthenticated = !!accessToken;

  const login = useCallback(
    async (email, password) => {
      const { data } = await loginApi(email, password);
      storeLogin(data);

      if (data.mustChangePassword) {
        navigate("/change-password");
        return data;
      }

      // Redirect based on role priority
      const roles = data.roles || [];
      if (roles.includes("ACADEMIC_COORDINATOR")) {
        navigate("/coordinator/dashboard");
      } else if (roles.includes("FACULTY_MENTOR")) {
        navigate("/mentor/dashboard");
      } else if (roles.includes("SUBJECT_TEACHER")) {
        navigate("/teacher/dashboard");
      } else if (roles.includes("STUDENT")) {
        navigate("/student/dashboard");
      } else {
        navigate("/dashboard");
      }

      return data;
    },
    [navigate, storeLogin]
  );

  const changePassword = useCallback(
    async (currentPassword, newPassword) => {
      const { data } = await changePasswordApi(currentPassword, newPassword);
      // Update token and user
      storeLogin(data);

      const roles = data.roles || [];
      if (roles.includes("ACADEMIC_COORDINATOR")) {
        navigate("/coordinator/dashboard");
      } else if (roles.includes("FACULTY_MENTOR")) {
        navigate("/mentor/dashboard");
      } else if (roles.includes("SUBJECT_TEACHER")) {
        navigate("/teacher/dashboard");
      } else if (roles.includes("STUDENT")) {
        navigate("/student/dashboard");
      } else {
        navigate("/dashboard");
      }

      return data;
    },
    [navigate, storeLogin]
  );

  const logout = useCallback(() => {
    storeLogout();
  }, [storeLogout]);

  const getDashboardPath = useCallback(() => {
    const role = getPrimaryRole();
    switch (role) {
      case "ACADEMIC_COORDINATOR":
        return "/coordinator/dashboard";
      case "FACULTY_MENTOR":
        return "/mentor/dashboard";
      case "SUBJECT_TEACHER":
        return "/teacher/dashboard";
      case "STUDENT":
        return "/student/dashboard";
      default:
        return "/login";
    }
  }, [getPrimaryRole]);

  return {
    user,
    isAuthenticated,
    login,
    logout,
    changePassword,
    hasRole,
    getPrimaryRole,
    getDashboardPath,
    activeView,
    setActiveView,
  };
}
