import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router";
import { Users, AlertTriangle, HeartHandshake, Flag } from "lucide-react";
import useUiStore from "../../stores/uiStore";
import { getMentorDashboard } from "../../api/mentor";
import Card, { CardTitle } from "../../components/ui/Card";
import StatCard from "../../components/ui/StatCard";
import RiskBadge from "../../components/ui/RiskBadge";
import Button from "../../components/ui/Button";
import LoadingSpinner from "../../components/ui/LoadingSpinner";
import RiskDistributionPie from "../../components/charts/RiskDistributionPie";

export default function MentorDashboard() {
  const setPageTitle = useUiStore((s) => s.setPageTitle);
  useEffect(() => setPageTitle("Mentor Dashboard"), [setPageTitle]);
  const navigate = useNavigate();

  const { data, isLoading } = useQuery({
    queryKey: ["mentor", "dashboard"],
    queryFn: async () => {
      const { data } = await getMentorDashboard();
      return data;
    },
  });

  if (isLoading) return <LoadingSpinner size="lg" className="mt-32" />;
  if (!data) return null;

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-text-primary">Mentor Dashboard</h2>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard title="Total Mentees" value={data.totalMentees || 0} icon={Users} />
        <StatCard title="At Risk" value={data.atRiskCount || 0} icon={AlertTriangle} />
        <StatCard title="Active Interventions" value={data.activeInterventionCount || 0} icon={HeartHandshake} />
        <StatCard title="Open Flags" value={data.openFlagCount || 0} icon={Flag} />
      </div>

      {/* Risk Distribution Pie */}
      {data.totalMentees > 0 && (
        <Card>
          <CardTitle>Mentee Risk Distribution</CardTitle>
          <RiskDistributionPie
            data={[
              { name: "HIGH", value: data.highRiskMentees || 0 },
              { name: "MEDIUM", value: data.mediumRiskMentees || 0 },
              { name: "LOW", value: data.lowRiskMentees || 0 },
            ]}
            height={280}
          />
        </Card>
      )}

      {/* At-Risk Mentees */}
      {data.atRiskMentees?.length > 0 && (
        <Card>
          <CardTitle>At-Risk Mentees</CardTitle>
          <div className="overflow-x-auto mt-4">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border-light">
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Name</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Roll No</th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Risk Score</th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Risk</th>
                  <th className="text-right px-4 py-3 text-xs font-semibold text-text-tertiary uppercase">Action</th>
                </tr>
              </thead>
              <tbody>
                {data.atRiskMentees.map((m) => (
                  <tr key={m.id || m.userId} className="border-b border-border-light last:border-0">
                    <td className="px-4 py-3 font-medium text-text-primary">{m.fullName}</td>
                    <td className="px-4 py-3 text-text-secondary">{m.rollNumber}</td>
                    <td className="px-4 py-3 text-right font-medium">{m.riskScore?.toFixed(1)}</td>
                    <td className="px-4 py-3 text-right"><RiskBadge label={m.riskLabel} /></td>
                    <td className="px-4 py-3 text-right">
                      <Button size="sm" variant="ghost" onClick={() => navigate(`/mentor/mentees/${m.id || m.userId}`)}>
                        View
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {/* Quick Actions */}
      <Card>
        <CardTitle>Quick Actions</CardTitle>
        <div className="flex flex-wrap gap-3 mt-4">
          <Button size="sm" onClick={() => navigate("/mentor/mentees")}>
            View All Mentees
          </Button>
          <Button size="sm" variant="secondary" onClick={() => navigate("/mentor/interventions")}>
            Interventions
          </Button>
          <Button size="sm" variant="secondary" onClick={() => navigate("/mentor/flags")}>
            Manage Flags
          </Button>
        </div>
      </Card>
    </div>
  );
}
