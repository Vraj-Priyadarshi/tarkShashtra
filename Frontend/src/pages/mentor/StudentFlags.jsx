import { useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Flag } from "lucide-react";
import toast from "react-hot-toast";
import useUiStore from "../../stores/uiStore";
import { getFlags, resolveFlag } from "../../api/mentor";
import Card from "../../components/ui/Card";
import Badge from "../../components/ui/Badge";
import Button from "../../components/ui/Button";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import EmptyState from "../../components/ui/EmptyState";
import { formatDate } from "../../lib/utils";

export default function StudentFlags() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Student Flags"), [setPageTitle]);
  const queryClient = useQueryClient();

  const { data: flags = [], isLoading } = useQuery({
    queryKey: ["mentor", "flags"],
    queryFn: async () => {
      const { data } = await getFlags();
      return data;
    },
  });

  const resolveMutation = useMutation({
    mutationFn: (flagId) => resolveFlag(flagId),
    onSuccess: () => {
      toast.success("Flag resolved!");
      queryClient.invalidateQueries({ queryKey: ["mentor", "flags"] });
    },
    onError: (err) => toast.error(err.response?.data?.message || "Failed to resolve flag"),
  });

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">Student Flags</h2>

      {flags.length === 0 ? (
        <EmptyState icon={Flag} title="No flags" description="No student flags to review." />
      ) : (
        <div className="space-y-4">
          {flags.map((flag) => (
            <Card key={flag.id}>
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <Badge variant={flag.resolved ? "success" : "warning"}>
                      {flag.resolved ? "Resolved" : "Unresolved"}
                    </Badge>
                  </div>
                  <p className="text-sm font-medium text-text-primary">{flag.studentName}</p>
                  <p className="text-xs text-text-secondary">{flag.subjectName}</p>
                  {flag.note && <p className="text-sm text-text-secondary mt-2">{flag.note}</p>}
                  <p className="text-xs text-text-tertiary mt-2">
                    Flagged by: {flag.flaggedByName} · {formatDate(flag.createdAt)}
                  </p>
                </div>
                {!flag.resolved && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => resolveMutation.mutate(flag.id)}
                    loading={resolveMutation.isPending}
                  >
                    Resolve
                  </Button>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
