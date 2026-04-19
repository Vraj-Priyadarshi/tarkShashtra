import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { Flag } from "lucide-react";
import useUiStore from "../../stores/uiStore";
import { getMyFlags } from "../../api/student";
import Card, { CardTitle } from "../../components/ui/Card";
import Badge from "../../components/ui/Badge";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import EmptyState from "../../components/ui/EmptyState";
import { formatDate } from "../../lib/utils";

export default function MyFlags() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("My Flags"), [setPageTitle]);

  const { data: flags = [], isLoading } = useQuery({
    queryKey: ["student", "my-flags"],
    queryFn: async () => {
      const { data } = await getMyFlags();
      return data;
    },
  });

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">My Flags</h2>

      {flags.length === 0 ? (
        <EmptyState
          icon={Flag}
          title="No flags!"
          description="Keep it up! No concerns have been flagged for you."
        />
      ) : (
        <div className="space-y-4">
          {flags.map((flag) => (
            <Card key={flag.id}>
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm font-medium text-text-primary">
                    {flag.subjectName}
                  </p>
                  <p className="text-xs text-text-secondary mt-1">
                    Flagged by: {flag.flaggedByName}
                  </p>
                  {flag.note && (
                    <p className="text-sm text-text-secondary mt-2">{flag.note}</p>
                  )}
                  <p className="text-xs text-text-tertiary mt-2">
                    {formatDate(flag.createdAt)}
                  </p>
                </div>
                <Badge variant={flag.resolved ? "success" : "warning"}>
                  {flag.resolved ? "Resolved" : "Unresolved"}
                </Badge>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
