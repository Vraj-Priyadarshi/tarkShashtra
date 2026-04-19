import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getUnreadCount, markAllRead, getNotifications } from "../api/notifications";

export default function useNotifications() {
  const queryClient = useQueryClient();

  const { data: unreadCount = 0 } = useQuery({
    queryKey: ["notifications", "unread-count"],
    queryFn: async () => {
      const { data } = await getUnreadCount();
      return data.count;
    },
    refetchInterval: 60000, // Poll every 60 seconds
  });

  const { mutate: markAllAsRead } = useMutation({
    mutationFn: markAllRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
  });

  return {
    unreadCount,
    markAllAsRead,
  };
}
