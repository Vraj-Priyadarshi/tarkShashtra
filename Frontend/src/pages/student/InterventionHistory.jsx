import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { HeartHandshake, CheckCircle, Clock } from "lucide-react";
import useUiStore from "../../stores/uiStore";
import { getInterventions } from "../../api/student";
import Card from "../../components/ui/Card";
import Badge from "../../components/ui/Badge";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import EmptyState from "../../components/ui/EmptyState";
import { formatDate } from "../../lib/utils";

const typeColors = {
  COUNSELLING_SESSION: "info",
  REMEDIAL_CLASS: "success",
  ASSIGNMENT_EXTENSION: "warning",
  PARENT_MEETING: "error",
  OTHER: "default",
};

export default function InterventionHistory() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Intervention History"), [setPageTitle]);

  const { data: interventions = [], isLoading } = useQuery({
    queryKey: ["student", "interventions"],
    queryFn: async () => {
      const { data } = await getInterventions();
      return data;
    },
  });

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">Intervention History</h2>

      {interventions.length === 0 ? (
        <EmptyState
          icon={HeartHandshake}
          title="No interventions"
          description="No interventions recorded yet."
        />
      ) : (
        <div className="space-y-4">
          {interventions.map((item) => (
            <Card key={item.id}>
              <div className="flex items-start justify-between mb-3">
                <div>
                  <Badge variant={typeColors[item.interventionType] || "default"}>
                    {item.interventionType?.replace(/_/g, " ")}
                  </Badge>
                  <p className="text-sm font-medium text-text-primary mt-2">
                    By: {item.mentorName}
                  </p>
                </div>
                <p className="text-xs text-text-tertiary">
                  {formatDate(item.interventionDate)}
                </p>
              </div>

              {item.remarks && (
                <p className="text-sm text-text-secondary mb-3">{item.remarks}</p>
              )}

              {/* Risk score change */}
              {item.preRiskScore != null && item.postRiskScore != null && (
                <div className="flex items-center gap-3 mb-3 text-sm">
                  <span className="text-text-tertiary">Risk:</span>
                  <span className="font-medium text-status-error">
                    {item.preRiskScore?.toFixed(1)}
                  </span>
                  <span className="text-text-tertiary">→</span>
                  <span className="font-medium text-status-success">
                    {item.postRiskScore?.toFixed(1)}
                  </span>
                  {item.scoreChange != null && (
                    <span
                      className={`font-medium ${
                        item.scoreChange < 0 ? "text-status-success" : "text-status-error"
                      }`}
                    >
                      ({item.scoreChange > 0 ? "+" : ""}
                      {item.scoreChange?.toFixed(1)})
                    </span>
                  )}
                </div>
              )}

              {/* Action items */}
              {item.actionItems?.length > 0 && (
                <div className="mt-3 space-y-2">
                  <p className="text-xs font-semibold text-text-tertiary uppercase">
                    Action Items
                  </p>
                  {item.actionItems.map((action) => (
                    <div key={action.id} className="flex items-center gap-2 text-sm">
                      {action.status === "COMPLETED" ? (
                        <CheckCircle className="w-4 h-4 text-status-success flex-shrink-0" />
                      ) : (
                        <Clock className="w-4 h-4 text-status-warning flex-shrink-0" />
                      )}
                      <span
                        className={
                          action.status === "COMPLETED"
                            ? "text-text-tertiary line-through"
                            : "text-text-primary"
                        }
                      >
                        {action.description}
                      </span>
                    </div>
                  ))}
                </div>
              )}

              {item.followUpDate && (
                <p className="text-xs text-text-tertiary mt-3">
                  Follow-up: {formatDate(item.followUpDate)}
                </p>
              )}
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
