import api from "./axios";

export const getNotifications = (page = 0, size = 20) =>
  api.get(`/notifications?page=${page}&size=${size}`);

export const getUnreadCount = () =>
  api.get("/notifications/unread-count");

export const markAllRead = () =>
  api.put("/notifications/mark-all-read");
