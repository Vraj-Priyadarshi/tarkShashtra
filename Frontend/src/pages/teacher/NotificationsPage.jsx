import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Bell } from "lucide-react";
import useUiStore from "../../stores/uiStore";
import { getNotifications, markAllRead } from "../../api/notifications";
import Card from "../../components/ui/Card";
import Button from "../../components/ui/Button";
import Badge from "../../components/ui/Badge";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import EmptyState from "../../components/ui/EmptyState";
import { formatDateTime } from "../../lib/utils";

const typeBadge = {
  HIGH_RISK_ALERT: "error",
  RISK_THRESHOLD_CROSSED: "warning",
  PRE_EXAM_ALERT: "info",
  DATA_ENTRY_REMINDER: "default",
  INTERVENTION_FOLLOW_UP: "success",
  STUDENT_FLAGGED: "warning",
  GENERAL: "default",
};

export default function NotificationsPage() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Notifications"), [setPageTitle]);
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["notifications", page],
    queryFn: async () => {
      const { data } = await getNotifications(page, 20);
      return data;
    },
  });

  const markAllMutation = useMutation({
    mutationFn: markAllRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["notifications"] }),
  });

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;

  const notifications = data?.content || [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-semibold text-text-primary">Notifications</h2>
        {notifications.length > 0 && (
          <Button variant="ghost" size="sm" onClick={() => markAllMutation.mutate()} loading={markAllMutation.isPending}>
            Mark all read
          </Button>
        )}
      </div>

      {notifications.length === 0 ? (
        <EmptyState icon={Bell} title="No notifications" description="You're all caught up!" />
      ) : (
        <div className="space-y-3">
          {notifications.map((notif) => (
            <Card key={notif.id} className={notif.read ? "opacity-60" : ""}>
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <Badge variant={typeBadge[notif.notificationType] || "default"}>
                      {notif.notificationType?.replace(/_/g, " ")}
                    </Badge>
                    {!notif.read && <span className="w-2 h-2 rounded-full bg-accent-primary" />}
                  </div>
                  <p className="text-sm font-medium text-text-primary">{notif.title}</p>
                  <p className="text-sm text-text-secondary mt-1">{notif.message}</p>
                </div>
                <p className="text-xs text-text-tertiary whitespace-nowrap ml-4">{formatDateTime(notif.createdAt)}</p>
              </div>
            </Card>
          ))}
        </div>
      )}

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-4">
          <Button variant="ghost" size="sm" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>Previous</Button>
          <span className="text-sm text-text-secondary">Page {page + 1} of {data.totalPages}</span>
          <Button variant="ghost" size="sm" onClick={() => setPage((p) => p + 1)} disabled={page + 1 >= data.totalPages}>Next</Button>
        </div>
      )}
    </div>
  );
}
