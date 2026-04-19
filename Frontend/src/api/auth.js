import api from "./axios";

export const login = (email, password) =>
  api.post("/auth/login", { email, password });

export const changePassword = (currentPassword, newPassword) =>
  api.post("/auth/change-password", { currentPassword, newPassword });

export const forgotPassword = (email) =>
  api.post("/auth/forgot-password", { email });

export const resetPassword = (token, newPassword) =>
  api.post("/auth/reset-password", { token, newPassword });

export const validateResetToken = (token) =>
  api.get(`/auth/validate-reset-token?token=${encodeURIComponent(token)}`);

export const healthCheck = () => api.get("/auth/health");
